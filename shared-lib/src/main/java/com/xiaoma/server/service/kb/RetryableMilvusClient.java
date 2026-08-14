/**
 * 可重试的 Milvus 客户端包装器。
 */
package com.xiaoma.server.service.kb;

import com.xiaoma.server.config.KbProperties;
import com.xiaoma.server.config.MilvusClientFactory;
import io.milvus.v2.client.MilvusClientV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.Closeable;

/**
 * 可重试的 Milvus 客户端包装器。
 * <p>
 * - 首次请求时才尝试创建底层 {@link MilvusClientV2}，避免启动时 Milvus 未就绪导致永久不可用。
 * - 创建失败后进入冷却期，冷却期内不再重试，防止高频请求打爆日志和 Milvus。
 * - Milvus 恢复后，下次请求自动重新创建并恢复使用。
 * - 容器关闭时调用 {@link #close()} 释放连接。
 */
@Component
public class RetryableMilvusClient implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(RetryableMilvusClient.class);

    private final MilvusClientFactory factory;
    private final long retryCooldownMs;

    private final Object lock = new Object();
    private volatile MilvusClientV2 clientRef;
    private volatile long lastFailureTime;
    private volatile boolean closed;

    /**
     * 构造 RetryableMilvusClient 实例。
     */
    public RetryableMilvusClient(MilvusClientFactory factory, KbProperties kbProperties) {
        this.factory = factory;
        Integer cooldown = kbProperties.getMilvus().getRetryCooldownMs();
        this.retryCooldownMs = cooldown != null && cooldown > 0 ? cooldown.longValue() : 30000L;
    }

    /**
     * 获取当前可用的 Milvus 客户端。
     *
     * @return 可用客户端；若当前不可用或已关闭则返回 null
     */
    public MilvusClientV2 getClient() {
        if (closed) {
            return null;
        }

        MilvusClientV2 client = clientRef;
        if (client != null) {
            return client;
        }

        synchronized (lock) {
            if (closed) {
                return null;
            }

            client = clientRef;
            if (client != null) {
                return client;
            }

            long now = System.currentTimeMillis();
            if (lastFailureTime > 0 && now - lastFailureTime < retryCooldownMs) {
                log.debug("Milvus client creation skipped due to retry cooldown");
                return null;
            }

            try {
                client = factory.createClient();
                if (client == null) {
                    lastFailureTime = now;
                    return null;
                }
                clientRef = client;
                lastFailureTime = 0;
                log.info("Milvus client initialized successfully");
                return client;
            } catch (Exception e) {
                lastFailureTime = now;
                log.warn("Milvus client creation failed, will retry after {} ms: {}", retryCooldownMs, e.getMessage());
                return null;
            }
        }
    }

    /**
     * 判断当前是否有可用的 Milvus 客户端。
     */
    public boolean isReady() {
        return clientRef != null && !closed;
    }

    /**
     * 重置客户端状态，下次请求会重新尝试创建。
     * 调用方应保证在关闭旧客户端后再调用此方法。
     */
    public void reset() {
        synchronized (lock) {
            clientRef = null;
            lastFailureTime = 0;
        }
    }

    /**
     * 重新初始化客户端：关闭当前客户端（如有），清空缓存，下次请求会按最新配置重新创建。
     */
    public void reinitialize() {
        MilvusClientV2 client;
        synchronized (lock) {
            if (closed) {
                log.warn("RetryableMilvusClient is closed, skipping reinitialize");
                return;
            }
            client = clientRef;
            clientRef = null;
            lastFailureTime = 0;
        }
        if (client != null) {
            try {
                client.close();
                log.info("Milvus client closed for reinitialize");
            } catch (Exception e) {
                log.warn("Failed to close Milvus client during reinitialize: {}", e.getMessage());
            }
        }
    }

    @Override
    public void close() {
        MilvusClientV2 client;
        synchronized (lock) {
            closed = true;
            client = clientRef;
            clientRef = null;
            lastFailureTime = 0;
        }
        if (client != null) {
            try {
                client.close();
                log.info("Milvus client closed");
            } catch (Exception e) {
                log.warn("Failed to close Milvus client: {}", e.getMessage());
            }
        }
    }
}
