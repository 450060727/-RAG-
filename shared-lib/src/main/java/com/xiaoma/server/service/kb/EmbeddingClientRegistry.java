/**
 * service/kb 模块的 EmbeddingClientRegistry 类/接口定义。
 */
package com.xiaoma.server.service.kb;

import com.xiaoma.server.common.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Embedding 客户端策略注册表。
 * 收集所有 {@link EmbeddingClient} 实现，按 provider 标识路由。
 */
@Component
/**
 * EmbeddingClientRegistry 类。
 */
public class EmbeddingClientRegistry {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingClientRegistry.class);

    // provider -> 客户端实现
    private final Map<String, EmbeddingClient> clients; // clients 字段

    /**
     * 构造 EmbeddingClientRegistry 实例。
     * @param embeddingClients 参数说明
     */
    public EmbeddingClientRegistry(List<EmbeddingClient> embeddingClients) {
        this.clients = embeddingClients.stream()
                .filter(c -> c.provider() != null && !c.provider().isBlank())
                .collect(Collectors.toMap(
                        c -> c.provider().toLowerCase(),
                        Function.identity(),
                        (a, b) -> {
                            log.warn("发现重复的 EmbeddingClient provider={}，保留 {}", a.provider(), a.getClass().getSimpleName());
                            return a;
                        }
                ));
        log.info("已加载 EmbeddingClient 策略: {}", clients.keySet());
    }

    /**
     * 根据 provider 获取对应客户端。
     *
     * @param provider provider 标识
     * @return 客户端实现
     * @throws BizException 找不到对应实现时抛出
     */
    public EmbeddingClient get(String provider) {
        String key = provider == null ? "" : provider.toLowerCase();
        EmbeddingClient client = clients.get(key);
        if (client == null) {
            throw new BizException("不支持的 embedding provider: " + provider);
        }
        return client;
    }

    /**
     * 判断指定 provider 是否已注册。
     *
     * @param provider provider 标识
     * @return true 表示已注册
     */
    public boolean supports(String provider) {
        return provider != null && clients.containsKey(provider.toLowerCase());
    }

    /**
     * 获取所有已注册的 provider 列表。
     *
     * @return provider 列表
     */
    public List<String> providers() {
        return List.copyOf(clients.keySet());
    }
}
