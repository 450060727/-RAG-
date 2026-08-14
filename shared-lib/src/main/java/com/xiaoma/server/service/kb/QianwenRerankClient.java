/**
 * service/kb 模块的 QianwenRerankClient 类/接口定义。
 */
package com.xiaoma.server.service.kb;

import com.alibaba.fastjson2.JSON;
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
 * 千问（DashScope OpenAI 兼容模式）Rerank 客户端。
 */
@Component
/**
 * QianwenRerankClient 类。
 */
public class QianwenRerankClient implements RerankClient {

    private static final Logger log = LoggerFactory.getLogger(QianwenRerankClient.class);

    /**
     * provider 方法。
     * @return 返回值说明
     */
    @Override
    public String provider() {
        return "qianwen";
    }
    private static final MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient client; // client 字段

    /**
     * 构造 QianwenRerankClient 实例。
     */
    public QianwenRerankClient() {
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
            throw new BizException("未配置千问 rerank API Key");
        }
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new BizException("未配置千问 rerank baseUrl");
        }
        if (model == null || model.isBlank()) {
            throw new BizException("未配置千问 rerank 模型");
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

        String url = baseUrl.replaceAll("/$", "") + "/rerank";
        RequestBody body = RequestBody.create(JSON.toJSONString(requestBody), JSON_TYPE);
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String rawJson = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                log.error("千问 rerank 请求失败: status={}, body={}", response.code(), rawJson);
                throw new BizException("千问 rerank 请求失败: HTTP " + response.code());
            }

            RerankResponse resp = JSON.parseObject(rawJson, RerankResponse.class);
            if (resp == null || resp.results == null) {
                throw new BizException("千问 rerank 返回格式异常");
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
            log.error("千问 rerank 调用异常", e);
            throw new BizException("千问 rerank 调用异常: " + e.getMessage());
        }
    }

    public static class RerankRequest {
        public String model; // model 字段
        public String query; // query 字段
        public List<String> documents; // documents 字段
        public Integer topN; // topN 字段
    }

    public static class RerankResponse {
        public List<RerankResult> results; // results 字段
    }

    public static class RerankResult {
        public Integer index; // index 字段
        public Double relevanceScore; // relevanceScore 字段
    }
}
