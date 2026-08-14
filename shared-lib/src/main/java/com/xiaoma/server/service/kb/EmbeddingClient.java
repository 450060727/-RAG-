package com.xiaoma.server.service.kb;

import com.xiaoma.server.entity.kb.KbModelConfig;

import java.util.List;

/**
 * Embedding 客户端统一接口。
 * 支持按单条文本或批量文本生成向量。
 */
public interface EmbeddingClient {

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
     * 将单段文本编码为向量（使用默认/当前全局配置）。
     *
     * @param text 输入文本
     * @return 向量数组
     */
    float[] embed(String text);

    /**
     * 批量编码（若提供者支持）。
     *
     * @param texts 输入文本列表
     * @return 向量数组列表
     */
    default List<float[]> embed(List<String> texts) {
        return texts.stream().map(this::embed).toList();
    }

    /**
     * 使用指定配置对单段文本编码。
     *
     * @param text   输入文本
     * @param config 模型配置
     * @return 向量数组
     */
    default float[] embed(String text, KbModelConfig config) {
        return embed(text);
    }

    /**
     * 使用指定配置批量编码。
     *
     * @param texts  输入文本列表
     * @param config 模型配置
     * @return 向量数组列表
     */
    default List<float[]> embed(List<String> texts, KbModelConfig config) {
        return texts.stream().map(t -> embed(t, config)).toList();
    }
}
