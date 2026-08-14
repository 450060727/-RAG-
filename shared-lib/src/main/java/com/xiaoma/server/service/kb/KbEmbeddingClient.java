/**
 * 知识库Embedding客户端实现类。
 */
package com.xiaoma.server.service.kb;

import com.xiaoma.server.common.BizException;
import com.xiaoma.server.config.KbProperties;
import org.springframework.stereotype.Component;

/**
 * 知识库Embedding客户端实现类。
 * 本类定义了 KbEmbeddingClient 的公共契约与数据结构。
 */
@Component
public class KbEmbeddingClient implements EmbeddingClient {

    private final KbProperties kbProperties; // kb 配置属性
    private final SiliconFlowEmbeddingClient siliconFlowClient; // siliconFlow 客户端
    private final OllamaEmbeddingClient ollamaClient; // ollama 客户端

    /**
     * 构造 KbEmbeddingClient 实例。
     */
    public KbEmbeddingClient(KbProperties kbProperties,
                             SiliconFlowEmbeddingClient siliconFlowClient,
                             OllamaEmbeddingClient ollamaClient) {
        this.kbProperties = kbProperties;
        this.siliconFlowClient = siliconFlowClient;
        this.ollamaClient = ollamaClient;
    }

    /**
     * embed 方法。
     * @param text 参数说明
     * @return 返回值说明
     */
    @Override
    public float[] embed(String text) {
        String provider = kbProperties.getEmbedding().getProvider();
        if ("ollama".equalsIgnoreCase(provider)) {
            return ollamaClient.embed(text);
        }
        if ("siliconflow".equalsIgnoreCase(provider)) {
            return siliconFlowClient.embed(text);
        }
        throw new BizException("不支持的 embedding provider: " + provider);
    }
}
