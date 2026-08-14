/**
 * 知识库Chat业务服务类。
 */
package com.xiaoma.server.service.kb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaoma.server.common.BizException;
import com.xiaoma.server.config.KbProperties;
import com.xiaoma.server.dto.kb.KbChatRequest;
import com.xiaoma.server.dto.kb.KbChatResponse;
import com.xiaoma.server.entity.kb.KbCategory;
import com.xiaoma.server.entity.kb.KbChatMessage;
import com.xiaoma.server.entity.kb.KbChatSession;
import com.xiaoma.server.entity.kb.KbModelConfig;
import com.xiaoma.server.mapper.kb.KbCategoryMapper;
import com.xiaoma.server.mapper.kb.KbChatMessageMapper;
import com.xiaoma.server.mapper.kb.KbChatSessionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 知识库Chat业务服务类。
 * 本类定义了 KbChatService 的公共契约与数据结构。
 */
@Service
public class KbChatService {

    private static final Logger log = LoggerFactory.getLogger(KbChatService.class);

    private final KbEmbeddingClient embeddingClient; // embedding 客户端
    private final MilvusService milvusService; // milvus 服务
    private final OllamaChatClient ollamaChatClient; // ollamaChat 客户端
    private final OllamaRerankClient ollamaRerankClient; // ollamaRerank 客户端
    private final KbChatSessionMapper kbChatSessionMapper; // kbChatSession 数据访问
    private final KbChatMessageMapper kbChatMessageMapper; // kbChatMessage 数据访问
    private final KbCategoryMapper kbCategoryMapper; // kbCategory 数据访问
    private final KbModelConfigService kbModelConfigService; // 模型配置服务
    private final KbProperties kbProperties; // kb 配置属性
    private final KbWriteBackService kbWriteBackService; // kbWriteBack 服务
    private final ObjectMapper objectMapper; // JSON 序列化工具

    /**
     * 构造 KbChatService 实例。
     */
    public KbChatService(KbEmbeddingClient embeddingClient, MilvusService milvusService,
                         OllamaChatClient ollamaChatClient, OllamaRerankClient ollamaRerankClient,
                         KbChatSessionMapper kbChatSessionMapper,
                         KbChatMessageMapper kbChatMessageMapper, KbCategoryMapper kbCategoryMapper,
                         KbModelConfigService kbModelConfigService,
                         KbProperties kbProperties, KbWriteBackService kbWriteBackService) {
        this.embeddingClient = embeddingClient;
        this.milvusService = milvusService;
        this.ollamaChatClient = ollamaChatClient;
        this.ollamaRerankClient = ollamaRerankClient;
        this.kbChatSessionMapper = kbChatSessionMapper;
        this.kbChatMessageMapper = kbChatMessageMapper;
        this.kbCategoryMapper = kbCategoryMapper;
        this.kbModelConfigService = kbModelConfigService;
        this.kbProperties = kbProperties;
        this.kbWriteBackService = kbWriteBackService;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 执行一次知识库对话。
     * 流程：校验分类 -> 创建/复用会话 -> 保存用户问题 -> 向量检索 -> Rerank/阈值过滤 ->
     * 构造 Prompt -> 调用大模型 -> 保存助手回答 -> 自动回写候选判断。
     *
     * @param uid 当前用户 ID
     * @param req 对话请求，包含问题、分类 ID、会话 ID 等
     * @return 对话响应，包含回答、是否命中本地知识、来源片段等
     */
    @Transactional
    public KbChatResponse chat(Long uid, KbChatRequest req) {
        // 未指定分类时使用默认分类
        Integer categoryId = req.categoryId() != null ? req.categoryId() : kbProperties.getChat().getDefaultCategoryId();
        KbCategory category = kbCategoryMapper.selectById(categoryId);
        // 分类不存在或非启用状态时拒绝回答
        if (category == null || category.getStatus() != 0) {
            throw new BizException("知识库分类不存在或已禁用");
        }
        KbModelConfig modelConfig = kbModelConfigService.current(category);

        // 会话处理：未传 sessionId 时新建会话
        Integer sessionId = req.sessionId();
        if (sessionId == null) {
            KbChatSession session = new KbChatSession();
            session.setUserId(uid.intValue());
            session.setCategoryId(categoryId);
            session.setTitle(truncate(req.question(), 30));
            session.setStatus(0);
            session.setCreatedAt(LocalDateTime.now());
            session.setUpdatedAt(LocalDateTime.now());
            kbChatSessionMapper.insert(session);
            sessionId = session.getId();
        }

        // 保存用户问题消息
        KbChatMessage userMsg = new KbChatMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setRole("user");
        userMsg.setContent(req.question());
        userMsg.setUseLocal(0);
        userMsg.setWriteBackStatus("NONE");
        userMsg.setCreatedAt(LocalDateTime.now());
        kbChatMessageMapper.insert(userMsg);

        // 本地检索：先计算问题向量，再按配置召回 TopK 候选；若启用 rerank 则多召回一些
        float[] questionVector = embeddingClient.embed(req.question());
        int recallTopK = Boolean.TRUE.equals(kbProperties.getChat().getRerankEnabled())
                ? kbProperties.getChat().getRerankTopK()
                : kbProperties.getChat().getTopK();
        List<MilvusService.SearchResult> rawHits = milvusService.search(categoryId, questionVector, recallTopK);

        // 确定本地知识判定阈值（优先使用分类级配置，否则使用全局默认值）
        double localThreshold = modelConfig.getLocalThreshold() != null
                ? modelConfig.getLocalThreshold().doubleValue()
                : kbProperties.getChat().getLocalThreshold();

        // 以最高相似度是否超过阈值作为是否使用本地知识的依据
        boolean useLocal = !rawHits.isEmpty() && rawHits.get(0).score >= localThreshold;
        float maxScore = rawHits.isEmpty() ? 0f : rawHits.get(0).score;

        log.info("知识库检索: categoryId={}, recallTopK={}, hits={}, maxScore={:.4f}, localThreshold={:.4f}, useLocal={}",
                categoryId, recallTopK, rawHits.size(), maxScore, localThreshold, useLocal);

        // Rerank 精排 或 上下文阈值过滤，得到最终进入 Prompt 的片段
        List<MilvusService.SearchResult> hits;
        if (Boolean.TRUE.equals(kbProperties.getChat().getRerankEnabled()) && !rawHits.isEmpty()) {
            hits = ollamaRerankClient.rerank(
                    req.question(),
                    rawHits,
                    kbProperties.getChat().getRerankLimit(),
                    kbProperties.getChat().getRerankPromptTemplate());
        } else {
            hits = filterHits(rawHits, kbProperties.getChat().getContextThreshold());
        }

        log.info("知识库最终命中: categoryId={}, hits={}, useLocal={}", categoryId, hits.size(), useLocal);

        // 构造 system / user Prompt；命中本地知识时把参考资料注入 user Prompt
        String systemPrompt = buildSystemPrompt(useLocal);
        String context = buildContext(hits, kbProperties.getChat().getContextMaxChars());
        String userPrompt = context.isEmpty() ? req.question() : "根据以下参考资料回答问题：\n" + context + "\n\n问题：" + req.question();

        // 查询会话历史，按独立字符预算截断
        List<Map<String, String>> history = buildHistory(sessionId);

        // 调用本地 Ollama 模型生成回答；命中本地时提供上下文，未命中则走通用问答
        OllamaChatClient.ChatCompletion completion = ollamaChatClient.chat(systemPrompt, history, userPrompt);
        String answer = completion.answer();
        String llmRaw = completion.rawJson();

        // 保存助手回答消息
        KbChatMessage assistantMsg = new KbChatMessage();
        assistantMsg.setSessionId(sessionId);
        assistantMsg.setRole("assistant");
        assistantMsg.setContent(answer);
        assistantMsg.setSourcesJson(toJson(buildSources(hits)));
        assistantMsg.setUseLocal(useLocal ? 1 : 0);
        assistantMsg.setConfidence(BigDecimal.valueOf(maxScore));
        assistantMsg.setLlmResponseJson(llmRaw);
        assistantMsg.setWriteBackStatus("NONE");
        assistantMsg.setCreatedAt(LocalDateTime.now());
        kbChatMessageMapper.insert(assistantMsg);

        // 自动回写候选：未命中本地知识且开启自动回写时，判断是否需要写入知识库
        if (!useLocal && modelConfig.getAutoWriteEnabled() != null && modelConfig.getAutoWriteEnabled() == 1) {
            double autoThreshold = modelConfig.getAutoWriteThreshold() != null
                    ? modelConfig.getAutoWriteThreshold().doubleValue()
                    : kbProperties.getChat().getLocalThreshold();
            if (maxScore < autoThreshold && kbWriteBackService.shouldAutoWrite(req.question(), answer)) {
                assistantMsg.setWriteBackStatus("PENDING");
                kbChatMessageMapper.updateById(assistantMsg);
            }
        }

        // 组装响应返回给前端
        return new KbChatResponse(
                assistantMsg.getId(),
                answer,
                useLocal,
                BigDecimal.valueOf(maxScore),
                buildSources(hits),
                sessionId
        );
    }

    private List<Map<String, String>> buildHistory(Integer sessionId) {
        int rounds = kbProperties.getChat().getHistoryRounds();
        int maxChars = kbProperties.getChat().getHistoryMaxChars();
        if (rounds <= 0 || maxChars <= 0) {
            return null;
        }

        // 查询当前会话最近 N 条 assistant/user 消息（不含本次刚插入的问题）
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<KbChatMessage>();
        wrapper.eq("session_id", sessionId)
                .in("role", "user", "assistant")
                .orderByDesc("id")
                .last("limit " + (rounds * 2));
        List<KbChatMessage> messages = kbChatMessageMapper.selectList(wrapper);
        if (messages == null || messages.isEmpty()) {
            return null;
        }

        // 按时间正序排列
        Collections.reverse(messages);

        List<Map<String, String>> history = new ArrayList<>();
        int usedChars = 0;
        for (KbChatMessage msg : messages) {
            String content = msg.getContent();
            if (content == null) {
                continue;
            }
            // 历史消息独立预算，按字符数截断
            if (usedChars + content.length() > maxChars) {
                break;
            }
            history.add(Map.of("role", msg.getRole(), "content", content));
            usedChars += content.length();
        }
        return history.isEmpty() ? null : history;
    }

    private String buildSystemPrompt(boolean useLocal) {
        if (useLocal) {
            return "你是一名基于本地知识库的问答助手。请严格根据提供的参考资料回答问题，若参考资料不足请明确说明。回答需简洁、准确，并标注信息来源。";
        }
        return "你是一名通用问答助手。请基于你的知识回答问题，回答需简洁、准确。";
    }

    private String buildContext(List<MilvusService.SearchResult> hits, int maxChars) {
        if (hits == null || hits.isEmpty() || maxChars <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int usedChars = 0;
        for (int i = 0; i < hits.size(); i++) {
            MilvusService.SearchResult h = hits.get(i);
            String title = h.title != null ? h.title : "未知来源";
            String line = "[" + (i + 1) + "] " + title + "：" + h.content + "\n";
            if (usedChars + line.length() > maxChars) {
                int remain = maxChars - usedChars;
                // 如果剩余空间还能容纳一条被截断的片段，则追加截断内容
                String prefix = "[" + (i + 1) + "] " + title + "：";
                int contentLimit = remain - prefix.length() - 1; // 预留换行符
                if (contentLimit > 20) {
                    sb.append(prefix).append(truncate(h.content, contentLimit)).append("\n");
                }
                break;
            }
            sb.append(line);
            usedChars += line.length();
        }
        return sb.toString();
    }

    private List<MilvusService.SearchResult> filterHits(List<MilvusService.SearchResult> hits, double threshold) {
        if (hits == null || hits.isEmpty()) {
            return Collections.emptyList();
        }
        List<MilvusService.SearchResult> filtered = hits.stream()
                .filter(h -> h.score >= threshold)
                .sorted((a, b) -> Float.compare(b.score, a.score))
                .toList();
        log.debug("检索命中 {} 条，经上下文阈值 {} 过滤后剩余 {} 条", hits.size(), threshold, filtered.size());
        return filtered;
    }

    private List<KbChatResponse.Source> buildSources(List<MilvusService.SearchResult> hits) {
        return hits.stream()
                .map(h -> new KbChatResponse.Source(h.docId, h.title, h.content, BigDecimal.valueOf(h.score)))
                .toList();
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() > maxLen ? s.substring(0, maxLen) + "..." : s;
    }
}
