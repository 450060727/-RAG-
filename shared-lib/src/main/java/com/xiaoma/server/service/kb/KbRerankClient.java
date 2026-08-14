/**
 * service/kb 模块的 KbRerankClient 类/接口定义。
 */
package com.xiaoma.server.service.kb;

import com.xiaoma.server.entity.kb.KbCategory;
import com.xiaoma.server.entity.kb.KbModelConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Rerank 客户端路由器。
 * 根据配置选择具体 provider，通过 {@link RerankClientRegistry} 查找实现。
 */
@Component
/**
 * KbRerankClient 类。
 */
public class KbRerankClient {

    private static final Logger log = LoggerFactory.getLogger(KbRerankClient.class);

    private final KbModelConfigService configService; // config 服务
    private final RerankClientRegistry rerankClientRegistry; // rerankClientRegistry 字段

    /**
     * 构造 KbRerankClient 实例。
     */
    public KbRerankClient(KbModelConfigService configService,
                          RerankClientRegistry rerankClientRegistry) {
        this.configService = configService;
        this.rerankClientRegistry = rerankClientRegistry;
    }

    public List<MilvusService.SearchResult> rerank(String query,
                                                      List<MilvusService.SearchResult> candidates,
                                                      int limit,
                                                      String promptTemplate) {
        return rerank(query, candidates, limit, promptTemplate, configService.current());
    }

    public List<MilvusService.SearchResult> rerank(String query,
                                                      List<MilvusService.SearchResult> candidates,
                                                      int limit,
                                                      String promptTemplate,
                                                      KbCategory category) {
        return rerank(query, candidates, limit, promptTemplate, configService.current(category));
    }

    public List<MilvusService.SearchResult> rerank(String query,
                                                      List<MilvusService.SearchResult> candidates,
                                                      int limit,
                                                      String promptTemplate,
                                                      KbModelConfig config) {
        if (config.getRerankEnabled() == null || config.getRerankEnabled() != 1) {
            return candidates;
        }
        if (candidates == null || candidates.isEmpty()) {
            return candidates;
        }
        String provider = config.getRerankProvider();
        if (provider == null || provider.isBlank()) {
            provider = "ollama";
        }
        log.debug("路由 rerank 请求到 provider: {}", provider);
        return rerankClientRegistry.get(provider).rerank(query, candidates, limit, promptTemplate, config);
    }
}
