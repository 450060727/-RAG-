package com.xiaoma.server.service.kb;

import com.xiaoma.server.entity.kb.KbModelConfig;

import java.util.List;
import java.util.Map;

/**
 * 对话模型客户端统一接口。
 */
public interface ChatClient {

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
     * 使用指定配置进行对话。
     *
     * @param systemPrompt 系统提示词
     * @param history      历史消息（role/content）
     * @param question     当前问题
     * @param config       模型配置
     * @return 回答与原始响应 JSON
     */
    ChatCompletion chat(String systemPrompt,
                        List<Map<String, String>> history,
                        String question,
                        KbModelConfig config);

    /**
     * 对话结果。
     *
     * @param answer  模型回答
     * @param rawJson 原始响应 JSON
     */
    record ChatCompletion(String answer, String rawJson) {
    }
}
