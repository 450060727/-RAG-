/**
 * SiliconFlowEmbedding客户端实现类。
 */
package com.xiaoma.server.service.kb;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaoma.server.common.BizException;
import com.xiaoma.server.config.KbProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * SiliconFlowEmbedding客户端实现类。
 * 本类定义了 SiliconFlowEmbeddingClient 的公共契约与数据结构。
 */
@Component
public class SiliconFlowEmbeddingClient implements EmbeddingClient {

    private static final Logger log = LoggerFactory.getLogger(SiliconFlowEmbeddingClient.class);

    private final KbProperties kbProperties; // kb 配置属性
    private final KbModelConfigService modelConfigService; // 模型配置服务
    private final RestTemplate restTemplate; // restTemplate 字段
    private final ObjectMapper objectMapper; // JSON 序列化工具

    /**
     * 构造 SiliconFlowEmbeddingClient 实例。
     * @param kbProperties 参数说明
     * @param modelConfigService 参数说明
     */
    public SiliconFlowEmbeddingClient(KbProperties kbProperties, KbModelConfigService modelConfigService) {
        this.kbProperties = kbProperties;
        this.modelConfigService = modelConfigService;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
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
        String apiKey = kbProperties.getEmbedding().getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new BizException("未配置 SiliconFlow API Key");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = Map.of(
                "model", kbProperties.getEmbedding().getModel(),
                "input", text,
                "encoding_format", "float"
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    kbProperties.getEmbedding().getBaseUrl() + "/embeddings",
                    request,
                    String.class
            );
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new BizException("Embedding 请求失败: " + response.getStatusCode());
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode data = root.path("data");
            if (data.isArray() && data.size() > 0) {
                JsonNode embeddingNode = data.get(0).path("embedding");
                float[] vector = new float[embeddingNode.size()];
                for (int i = 0; i < embeddingNode.size(); i++) {
                    vector[i] = (float) embeddingNode.get(i).asDouble();
                }
                return vector;
            }
            throw new BizException("Embedding 返回格式异常");
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("SiliconFlow embedding error", e);
            throw new BizException("Embedding 调用异常: " + e.getMessage());
        }
    }

    /**
     * embed 方法。
     * @param texts 参数说明
     * @return 返回值说明
     */
    @Override
    public List<float[]> embed(List<String> texts) {
        return texts.stream().map(this::embed).toList();
    }
}
