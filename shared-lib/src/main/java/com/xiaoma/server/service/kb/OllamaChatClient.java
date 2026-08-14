/**
 * OllamaChat客户端实现类。
 */
package com.xiaoma.server.service.kb;

import com.alibaba.fastjson2.JSON;
import com.xiaoma.server.common.BizException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 本地 Ollama 对话客户端。
 * <p>
 * 调用 Ollama /api/chat 端点，使用 okHttp + fastjson2。
 * 默认模型为 qwen:1.8b，适合本地 CPU/GPU 推理。
 */
@Component
public class OllamaChatClient {

    private static final Logger log = LoggerFactory.getLogger(OllamaChatClient.class);
    private static final MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");

    @Value("${ollama.base-url:http://127.0.0.1:11434}")
    private String baseUrl; // baseUrl 字段

    @Value("${ollama.chat-model:qwen:1.8b}")
    private String chatModel; // chatModel 字段

    private final OkHttpClient client; // client 字段

    /**
     * 构造 OllamaChatClient 实例。
     */
    public OllamaChatClient() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(180, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    /**
     * ChatCompletion 方法。
     * @param answer 参数说明
     * @param rawJson 参数说明
     * @return 返回值说明
     */
    public record ChatCompletion(String answer, String rawJson) {
    }

    /**
     * chat 方法。
     * @param systemPrompt 参数说明
     * @param history 参数说明
     * @param question 参数说明
     * @return 返回值说明
     */
    public ChatCompletion chat(String systemPrompt, List<Map<String, String>> history, String question) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new BizException("未配置 Ollama 服务地址（ollama.base-url）");
        }
        if (chatModel == null || chatModel.isBlank()) {
            throw new BizException("未配置 Ollama 对话模型（ollama.chat-model）");
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
        requestBody.model = chatModel;
        requestBody.messages = messages;
        requestBody.stream = false;

        String url = baseUrl.replaceAll("/$", "") + "/api/chat";
        RequestBody body = RequestBody.create(JSON.toJSONString(requestBody), JSON_TYPE);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                String err = response.body() != null ? response.body().string() : "";
                log.error("Ollama chat error: status={}, body={}", response.code(), err);
                throw new BizException("Ollama 对话请求失败: HTTP " + response.code());
            }

            String rawJson = response.body().string();
            ChatResponse chatResp = JSON.parseObject(rawJson, ChatResponse.class);
            if (chatResp == null || chatResp.message == null) {
                throw new BizException("Ollama 对话返回格式异常");
            }

            String answer = chatResp.message.content != null ? chatResp.message.content : "";
            return new ChatCompletion(answer, rawJson);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ollama chat error", e);
            throw new BizException("Ollama 对话调用异常: " + e.getMessage());
        }
    }

    public static class ChatRequest {
        public String model; // model 字段
        public List<Message> messages; // messages 字段
        public Boolean stream; // stream 字段
    }

    public static class ChatResponse {
        public String model; // model 字段
        public Message message; // message 字段
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
}
