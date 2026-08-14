/**
 * OllamaEmbedding客户端实现类。
 */
package com.xiaoma.server.service.kb;

import com.alibaba.fastjson2.JSON;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.xiaoma.server.common.BizException;
import com.xiaoma.server.config.KbProperties;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 本地 Ollama Embedding 客户端。
 * <p>
 * 调用 Ollama /api/embed 端点，使用 okHttp + fastjson2，并带 caffeine 本地缓存。
 * 默认模型为 qwen3-embedding:0.6b-q8_0，输出维度 1024
 */
@Component
public class OllamaEmbeddingClient implements EmbeddingClient {

    private static final Logger log = LoggerFactory.getLogger(OllamaEmbeddingClient.class);
    private static final MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");

    private final KbProperties kbProperties; // kb 配置属性
    private final KbModelConfigService modelConfigService; // 模型配置服务
    private final OkHttpClient client; // client 字段
    private final Cache<String, float[]> embedCache; // embedCache 字段

    /**
     * 构造 OllamaEmbeddingClient 实例。
     * @param kbProperties 参数说明
     * @param modelConfigService 参数说明
     */
    public OllamaEmbeddingClient(KbProperties kbProperties, KbModelConfigService modelConfigService) {
        this.kbProperties = kbProperties;
        this.modelConfigService = modelConfigService;
        this.client = new OkHttpClient();
        this.embedCache = Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(12, TimeUnit.HOURS)
                .recordStats()
                .build();
    }

    private int vectorDim() {
        Integer dim = modelConfigService.current().getMilvusVectorDim();
        return dim != null ? dim : kbProperties.getMilvus().getVectorDim();
    }

    /**
     * embed 方法。
     * @param text 参数说明
     * @return 返回值说明
     */
    @Override
    public float[] embed(String text) {
        if (text == null || text.isBlank()) {
            return new float[vectorDim()];
        }

        float[] cached = embedCache.getIfPresent(text);
        if (cached != null) {
            return cached;
        }

        List<float[]> result = fetchEmbeddings(List.of(text));
        float[] vector = result.get(0);
        embedCache.put(text, vector);
        return vector;
    }

    /**
     * embed 方法。
     * @param texts 参数说明
     * @return 返回值说明
     */
    @Override
    public List<float[]> embed(List<String> texts) {
        List<float[]> results = new ArrayList<>(texts.size());
        List<Integer> missingIndexes = new ArrayList<>();
        List<String> missingTexts = new ArrayList<>();

        for (int i = 0; i < texts.size(); i++) {
            String text = texts.get(i);
            if (text == null || text.isBlank()) {
                results.add(new float[vectorDim()]);
                continue;
            }
            float[] cached = embedCache.getIfPresent(text);
            if (cached != null) {
                results.add(cached);
            } else {
                results.add(null); // 占位，稍后回填
                missingIndexes.add(i);
                missingTexts.add(text);
            }
        }

        if (!missingTexts.isEmpty()) {
            List<float[]> fetched = fetchEmbeddings(missingTexts);
            for (int i = 0; i < missingIndexes.size(); i++) {
                int idx = missingIndexes.get(i);
                float[] vector = fetched.get(i);
                results.set(idx, vector);
                embedCache.put(missingTexts.get(i), vector);
            }
        }

        return results;
    }

    private List<float[]> fetchEmbeddings(List<String> texts) {
        String url = kbProperties.getEmbedding().getOllamaUrl();
        String model = kbProperties.getEmbedding().getOllamaModel();
        if (model == null || model.isBlank()) {
            throw new BizException("未配置 Ollama embedding 模型（kb.embedding.ollama-model）");
        }
        if (url == null || url.isBlank()) {
            throw new BizException("未配置 Ollama URL（kb.embedding.ollama-url）");
        }

        EmbedRequest requestBody = new EmbedRequest();
        requestBody.model = model;
        requestBody.input = texts;

        RequestBody body = RequestBody.create(JSON.toJSONString(requestBody), JSON_TYPE);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new BizException("Ollama embedding 请求失败: " + response.code());
            }

            String respJson = response.body().string();
            EmbedResponse embedResp = JSON.parseObject(respJson, EmbedResponse.class);
            if (embedResp == null || embedResp.embeddings == null || embedResp.embeddings.size() != texts.size()) {
                throw new BizException("Ollama embedding 返回格式异常");
            }

            List<float[]> vectors = new ArrayList<>(texts.size());
            for (List<Double> embedding : embedResp.embeddings) {
                vectors.add(toFloatArray(embedding));
            }
            return vectors;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ollama embedding error", e);
            throw new BizException("Ollama embedding 调用异常: " + e.getMessage());
        }
    }

    private float[] toFloatArray(List<Double> list) {
        float[] arr = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i).floatValue();
        }
        return arr;
    }

    /**
     * 手动清理单条缓存（文本修改后调用）
     */
    public void clearCache(String text) {
        embedCache.invalidate(text);
    }

    /**
     * 清空全部缓存
     */
    public void clearAllCache() {
        embedCache.invalidateAll();
    }

    public static class EmbedRequest {
        public String model; // model 字段
        public List<String> input; // input 字段
    }

    public static class EmbedResponse {
        public String model; // model 字段
        public List<List<Double>> embeddings; // embeddings 字段
    }
}
