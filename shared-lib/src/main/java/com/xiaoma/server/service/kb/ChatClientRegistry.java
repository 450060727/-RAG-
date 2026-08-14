/**
 * service/kb 模块的 ChatClientRegistry 类/接口定义。
 */
package com.xiaoma.server.service.kb;

import com.xiaoma.server.common.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Chat 客户端策略注册表。
 * 收集所有 {@link ChatClient} 实现，按 provider 标识路由，新增厂商只需注册为 Spring Bean 即可。
 */
@Component
/**
 * ChatClientRegistry 类。
 */
public class ChatClientRegistry {

    private static final Logger log = LoggerFactory.getLogger(ChatClientRegistry.class);

    // provider -> 客户端实现
    private final Map<String, ChatClient> clients; // clients 字段

    /**
     * 构造 ChatClientRegistry 实例。
     * @param chatClients 参数说明
     */
    public ChatClientRegistry(List<ChatClient> chatClients) {
        this.clients = chatClients.stream()
                .filter(c -> c.provider() != null && !c.provider().isBlank())
                .collect(Collectors.toMap(
                        c -> c.provider().toLowerCase(),
                        Function.identity(),
                        (a, b) -> {
                            log.warn("发现重复的 ChatClient provider={}，保留 {}", a.provider(), a.getClass().getSimpleName());
                            return a;
                        }
                ));
        log.info("已加载 ChatClient 策略: {}", clients.keySet());
    }

    /**
     * 根据 provider 获取对应客户端。
     *
     * @param provider provider 标识
     * @return 客户端实现
     * @throws BizException 找不到对应实现时抛出
     */
    public ChatClient get(String provider) {
        String key = provider == null ? "" : provider.toLowerCase();
        ChatClient client = clients.get(key);
        if (client == null) {
            throw new BizException("不支持的 chat provider: " + provider);
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
