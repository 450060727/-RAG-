/**
 * service/kb 模块的 SiliconFlowRerankClient 类/接口定义。
 */
package com.xiaoma.server.service.kb;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import com.xiaoma.server.common.BizException;
import com.xiaoma.server.entity.kb.KbModelConfig;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 硅基流动（SiliconFlow OpenAI 兼容模式）Rerank 客户端。
 */
@Component
/**
 * SiliconFlowRerankClient 类。
 */
public class SiliconFlowRerankClient implements RerankClient {

    private static final Logger log = LoggerFactory.getLogger(SiliconFlowRerankClient.class);

    /**
     * provider 方法。
     * @return 返回值说明
     */
    @Override
    public String provider() {
        return "siliconflow";
    }
    private static final MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient client; // client 字段

    /**
     * 构造 SiliconFlowRerankClient 实例。
     */
    public SiliconFlowRerankClient() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public List<MilvusService.SearchResult> rerank(String query,
                                                      List<MilvusService.SearchResult> candidates,
                                                      int limit,
                                                      String promptTemplate,
                                                      KbModelConfig config) {
        String apiKey = config.getRerankApiKey();
        String baseUrl = config.getRerankBaseUrl();
        String model = config.getRerankModel();
        if (apiKey == null || apiKey.isBlank()) {
            throw new BizException("未配置 SiliconFlow rerank API Key");
        }
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new BizException("未配置 SiliconFlow rerank baseUrl");
        }
        if (model == null || model.isBlank()) {
            throw new BizException("未配置 SiliconFlow rerank 模型");
        }
        if (candidates == null || candidates.isEmpty()) {
            return candidates;
        }

        List<String> documents = candidates.stream()
                .map(r -> r.content != null ? r.content : "")
                .toList();

        RerankRequest requestBody = new RerankRequest();
        requestBody.model = model;
        requestBody.query = query;
        requestBody.documents = documents;
        requestBody.topN = Math.min(limit, candidates.size());
        requestBody.maxChunksPerDoc = config.getRerankMaxChunksPerDoc() != null
                ? config.getRerankMaxChunksPerDoc()
                : 512;

        String url = buildUrl(baseUrl, "/rerank");
        String requestJson = JSON.toJSONString(requestBody);
        RequestBody body = RequestBody.create(requestJson, JSON_TYPE);
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String rawJson = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                log.error("SiliconFlow rerank 请求失败: status={}, url={}, body={}, request={}",
                        response.code(), url, rawJson, requestJson);
                throw new BizException("SiliconFlow rerank 请求失败: HTTP " + response.code());
            }

            RerankResponse resp = JSON.parseObject(rawJson, RerankResponse.class);
            if (resp == null || resp.results == null) {
                throw new BizException("SiliconFlow rerank 返回格式异常");
            }

            List<MilvusService.SearchResult> result = new ArrayList<>();
            for (RerankResult r : resp.results) {
                int idx = r.index;
                if (idx < 0 || idx >= candidates.size()) {
                    continue;
                }
                MilvusService.SearchResult candidate = candidates.get(idx);
                candidate.score = r.relevanceScore.floatValue();
                result.add(candidate);
            }
            result.sort(Comparator.comparingDouble((MilvusService.SearchResult r) -> r.score).reversed());
            return result;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("SiliconFlow rerank 调用异常", e);
            throw new BizException("SiliconFlow rerank 调用异常: " + e.getMessage());
        }
    }

    public static class RerankRequest {
        public String model; // model 字段
        public String query; // query 字段
        public List<String> documents; // documents 字段
        @JSONField(name = "top_n")
        public Integer topN; // topN 字段
        @JSONField(name = "max_chunks_per_doc")
        public Integer maxChunksPerDoc = 512;
    }

    public static class RerankResponse {
        public List<RerankResult> results; // results 字段
    }

    public static class RerankResult {
        public Integer index; // index 字段
        @JSONField(name = "relevance_score")
        public Double relevanceScore; // relevanceScore 字段
    }

    private static String buildUrl(String baseUrl, String path) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return path;
        }
        String normalized = baseUrl.replaceAll("/$", "");
        if (normalized.endsWith(path)) {
            return normalized;
        }
        return normalized + path;
    }
}
