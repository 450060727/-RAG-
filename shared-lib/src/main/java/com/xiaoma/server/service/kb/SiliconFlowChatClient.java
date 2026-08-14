/**
 * service/kb 模块的 SiliconFlowChatClient 类/接口定义。
 */
package com.xiaoma.server.service.kb;

import com.alibaba.fastjson2.JSON;
import com.xiaoma.server.common.BizException;
import com.xiaoma.server.entity.kb.KbModelConfig;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 硅基流动（SiliconFlow OpenAI 兼容模式）对话客户端。
 */
@Component
/**
 * SiliconFlowChatClient 类。
 */
public class SiliconFlowChatClient implements ChatClient {

    private static final Logger log = LoggerFactory.getLogger(SiliconFlowChatClient.class);

    /**
     * provider 方法。
     * @return 返回值说明
     */
    @Override
    public String provider() {
        return "siliconflow";
    }
    private static final MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient client; // client 字段

    /**
     * 构造 SiliconFlowChatClient 实例。
     */
    public SiliconFlowChatClient() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    @Override
    /**
     * chat 方法。
     * @return 返回值说明
     */
    public ChatCompletion chat(String systemPrompt,
                               List<Map<String, String>> history,
                               String question,
                               KbModelConfig config) {
        String apiKey = config.getChatApiKey();
        String baseUrl = config.getChatBaseUrl();
        String model = config.getChatModel();
        if (apiKey == null || apiKey.isBlank()) {
            throw new BizException("未配置 SiliconFlow 对话 API Key");
        }
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new BizException("未配置 SiliconFlow 对话 baseUrl");
        }
        if (model == null || model.isBlank()) {
            throw new BizException("未配置 SiliconFlow 对话模型");
        }

        List<Message> messages = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            messages.add(new Message("system", systemPrompt));
        }
        if (history != null) {
            for (Map<String, String> h : history) {
                String role = h.get("role");
                String content = h.get("content");
                if (role == null || content == null) {
                    continue;
                }
                messages.add(new Message(role.toLowerCase(), content));
            }
        }
        messages.add(new Message("user", question));

        ChatRequest requestBody = new ChatRequest();
        requestBody.model = model;
        requestBody.messages = messages;
        if (config.getChatTemperature() != null) {
            requestBody.temperature = config.getChatTemperature().doubleValue();
        }
        if (config.getChatMaxTokens() != null) {
            requestBody.maxTokens = config.getChatMaxTokens();
        }

        String url = buildUrl(baseUrl, "/chat/completions");
        RequestBody body = RequestBody.create(JSON.toJSONString(requestBody), JSON_TYPE);
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String rawJson = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                log.error("SiliconFlow 对话请求失败: status={}, body={}", response.code(), rawJson);
                throw new BizException("SiliconFlow 对话请求失败: HTTP " + response.code());
            }

            ChatResponse resp = JSON.parseObject(rawJson, ChatResponse.class);
            if (resp == null || resp.choices == null || resp.choices.isEmpty()
                    || resp.choices.get(0).message == null) {
                throw new BizException("SiliconFlow 对话返回格式异常");
            }
            String answer = resp.choices.get(0).message.content;
            return new ChatCompletion(answer == null ? "" : answer, rawJson);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("SiliconFlow 对话调用异常", e);
            throw new BizException("SiliconFlow 对话调用异常: " + e.getMessage());
        }
    }

    public static class ChatRequest {
        public String model; // model 字段
        public List<Message> messages; // messages 字段
        public Double temperature; // temperature 字段
        public Integer maxTokens; // maxTokens 字段
    }

    public static class Message {
        public String role; // role 字段
        public String content; // content 字段

        public Message() {
        }

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    public static class ChatResponse {
        public List<Choice> choices; // choices 字段
    }

    public static class Choice {
        public Message message; // message 字段
    }

    private static String buildUrl(String baseUrl, String path) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return path;
        }
        String normalized = baseUrl.replaceAll("/$", "");
        if (normalized.endsWith(path)) {
            return normalized;
        }
        return normalized + path;
    }
}
