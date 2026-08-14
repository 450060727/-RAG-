/**
 * Milvus配置类。
 */
package com.xiaoma.server.config;

import com.xiaoma.server.service.kb.RetryableMilvusClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Milvus配置类。
 * 本类定义了 Milvus 相关 Bean 的暴露。
 */
@Configuration
public class MilvusConfig {

    private static final Logger log = LoggerFactory.getLogger(MilvusConfig.class);

    /**
     * Milvus 客户端工厂。
     */
    @Bean
    public MilvusClientFactory milvusClientFactory(KbProperties kbProperties,
                                                   com.xiaoma.server.service.kb.KbModelConfigService modelConfigService) {
        return new MilvusClientFactory(kbProperties, modelConfigService);
    }

    /**
     * 可重试的 Milvus 客户端包装器。
     * 容器关闭时自动释放底层连接。
     */
    @Bean(destroyMethod = "close")
    public RetryableMilvusClient retryableMilvusClient(MilvusClientFactory milvusClientFactory,
                                                       KbProperties kbProperties) {
        log.info("Creating RetryableMilvusClient with retry cooldown {} ms",
                kbProperties.getMilvus().getRetryCooldownMs());
        return new RetryableMilvusClient(milvusClientFactory, kbProperties);
    }
}
