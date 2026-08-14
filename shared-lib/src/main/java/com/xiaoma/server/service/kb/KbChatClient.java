/**
 * service/kb 模块的 KbChatClient 类/接口定义。
 */
package com.xiaoma.server.service.kb;

import com.xiaoma.server.entity.kb.KbCategory;
import com.xiaoma.server.entity.kb.KbModelConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Chat 客户端路由器。
 * 根据配置选择具体 provider，通过 {@link ChatClientRegistry} 查找实现。
 */
@Component
/**
 * KbChatClient 类。
 */
public class KbChatClient {

    private static final Logger log = LoggerFactory.getLogger(KbChatClient.class);

    private final KbModelConfigService configService; // config 服务
    private final ChatClientRegistry chatClientRegistry; // chatClientRegistry 字段

    /**
     * 构造 KbChatClient 实例。
     */
    public KbChatClient(KbModelConfigService configService,
                        ChatClientRegistry chatClientRegistry) {
        this.configService = configService;
        this.chatClientRegistry = chatClientRegistry;
    }

    /**
     * chat 方法。
     * @return 返回值说明
     */
    public ChatClient.ChatCompletion chat(String systemPrompt,
                               List<Map<String, String>> history,
                               String question) {
        return chat(systemPrompt, history, question, configService.current());
    }

    /**
     * chat 方法。
     * @return 返回值说明
     */
    public ChatClient.ChatCompletion chat(String systemPrompt,
                               List<Map<String, String>> history,
                               String question,
                               KbCategory category) {
        return chat(systemPrompt, history, question, configService.current(category));
    }

    /**
     * chat 方法。
     * @return 返回值说明
     */
    public ChatClient.ChatCompletion chat(String systemPrompt,
                               List<Map<String, String>> history,
                               String question,
                               KbModelConfig config) {
        String provider = config.getChatProvider();
        if (provider == null || provider.isBlank()) {
            provider = "ollama";
        }
        log.debug("路由 chat 请求到 provider: {}", provider);
        return chatClientRegistry.get(provider).chat(systemPrompt, history, question, config);
    }
}
