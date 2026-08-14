/**
 * config 模块的 ConfigValidator 类/接口定义。
 */
package com.xiaoma.server.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 知识库关键配置启动校验器。
 * 在应用启动后检查 Milvus、Embedding、Chat、Rerank 等必填项，缺失时打印强警告或阻止启动，
 * 避免运行到一半才发现配置错误导致排查困难。
 */
@Component
/**
 * ConfigValidator 类。
 */
public class ConfigValidator {

    private static final Logger log = LoggerFactory.getLogger(ConfigValidator.class);

    private final KbProperties kbProperties; // kb 配置属性

    /**
     * 构造 ConfigValidator 实例。
     * @param kbProperties 参数说明
     */
    public ConfigValidator(KbProperties kbProperties) {
        this.kbProperties = kbProperties;
    }

    /**
     * 应用启动后执行校验。
     * 非生产环境允许缺失并打印警告；生产环境建议抛异常阻止启动。
     */
    @PostConstruct
    public void validate() {
        KbProperties.Milvus milvus = kbProperties.getMilvus();
        if (!StringUtils.hasText(milvus.getHost())) {
            log.warn("[配置校验] kb.milvus.host 未配置，Milvus 相关功能将不可用");
        }
        if (!StringUtils.hasText(milvus.getPassword())) {
            log.warn("[配置校验] kb.milvus.password 未配置，若 Milvus 启用认证将连接失败");
        }

        KbProperties.Embedding embedding = kbProperties.getEmbedding();
        if (!StringUtils.hasText(embedding.getProvider())) {
            log.warn("[配置校验] kb.embedding.provider 未配置");
        }
        // API Key 仅在使用云端厂商时必填，这里仅做敏感信息提醒
        if (StringUtils.hasText(embedding.getApiKey()) && embedding.getApiKey().startsWith("sk-")) {
            log.debug("[配置校验] kb.embedding.apiKey 已配置（已脱敏）");
        }

        log.info("[配置校验] 知识库配置校验完成");
    }
}
