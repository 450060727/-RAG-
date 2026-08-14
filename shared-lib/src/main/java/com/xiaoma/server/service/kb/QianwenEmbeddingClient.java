/**
 * service/kb 模块的 QianwenEmbeddingClient 类/接口定义。
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 千问（DashScope OpenAI 兼容模式）Embedding 客户端。
 */
@Component
/**
 * QianwenEmbeddingClient 类。
 */
public class QianwenEmbeddingClient implements EmbeddingClient {

    private static final Logger log = LoggerFactory.getLogger(QianwenEmbeddingClient.class);

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
     * 构造 QianwenEmbeddingClient 实例。
     */
    public QianwenEmbeddingClient() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    /**
     * embed 方法。
     * @param text 参数说明
     * @return 返回值说明
     */
    @Override
    public float[] embed(String text) {
        throw new BizException("QianwenEmbeddingClient 需要传入 KbModelConfig 才能工作");
    }

    /**
     * embed 方法。
     * @param texts 参数说明
     * @return 返回值说明
     */
    @Override
    public List<float[]> embed(List<String> texts) {
        throw new BizException("QianwenEmbeddingClient 需要传入 KbModelConfig 才能工作");
    }

    /**
     * embed 方法。
     * @param text 参数说明
     * @param config 参数说明
     * @return 返回值说明
     */
    @Override
    public float[] embed(String text, KbModelConfig config) {
        if (text == null || text.isBlank()) {
            return new float[config.getMilvusVectorDim()];
        }
        List<float[]> results = embed(List.of(text), config);
        return results.get(0);
    }

    /**
     * embed 方法。
     * @param texts 参数说明
     * @param config 参数说明
     * @return 返回值说明
     */
    @Override
    public List<float[]> embed(List<String> texts, KbModelConfig config) {
        String apiKey = config.getEmbeddingApiKey();
        String baseUrl = config.getEmbeddingQianwenUrl();
        String model = config.getEmbeddingModel();
        if (apiKey == null || apiKey.isBlank()) {
            throw new BizException("未配置千问 embedding API Key");
        }
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new BizException("未配置千问 embedding baseUrl");
        }
        if (model == null || model.isBlank()) {
            throw new BizException("未配置千问 embedding 模型");
        }

        String url = baseUrl.replaceAll("/$", "") + "/embeddings";
        Map<String, Object> body = Map.of(
                "model", model,
                "input", texts,
                "encoding_format", "float"
        );
        RequestBody requestBody = RequestBody.create(JSON.toJSONString(body), JSON_TYPE);
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + apiKey)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String rawJson = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                log.error("千问 embedding 请求失败: status={}, body={}", response.code(), rawJson);
                throw new BizException("千问 embedding 请求失败: HTTP " + response.code());
            }

            EmbedResponse resp = JSON.parseObject(rawJson, EmbedResponse.class);
            if (resp == null || resp.data == null || resp.data.size() != texts.size()) {
                throw new BizException("千问 embedding 返回格式异常");
            }

            List<float[]> vectors = new ArrayList<>(texts.size());
            for (EmbedData data : resp.data) {
                if (data.embedding == null) {
                    throw new BizException("千问 embedding 返回格式异常");
                }
                float[] vector = new float[data.embedding.size()];
                for (int i = 0; i < data.embedding.size(); i++) {
                    vector[i] = data.embedding.get(i).floatValue();
                }
                vectors.add(vector);
            }
            return vectors;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("千问 embedding 调用异常", e);
            throw new BizException("千问 embedding 调用异常: " + e.getMessage());
        }
    }

    public static class EmbedResponse {
        public List<EmbedData> data; // data 字段
    }

    public static class EmbedData {
        public List<Double> embedding; // embedding 字段
        public Integer index; // index 字段
    }
}
