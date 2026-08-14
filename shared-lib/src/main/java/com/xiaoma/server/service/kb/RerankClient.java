package com.xiaoma.server.service.kb;

import com.xiaoma.server.entity.kb.KbModelConfig;

import java.util.List;

/**
 * Rerank 客户端统一接口。
 */
public interface RerankClient {

    /**
     * 返回当前客户端支持的 provider 标识，如 "ollama"、"qianwen"、"siliconflow"。
     * 用于策略注册表自动路由；返回 null 表示不参与自动注册。
     *
     * @return provider 标识
     */
    default String provider() {
        return null;
    }

    /**
     * 对候选片段进行重排。
     *
     * @param query          用户问题
     * @param candidates     向量召回候选
     * @param limit          返回条数上限
     * @param promptTemplate prompt 模板（本地 Ollama 使用）
     * @param config         模型配置
     * @return 重排后的候选列表
     */
    List<MilvusService.SearchResult> rerank(String query,
                                              List<MilvusService.SearchResult> candidates,
                                              int limit,
                                              String promptTemplate,
                                              KbModelConfig config);
}
