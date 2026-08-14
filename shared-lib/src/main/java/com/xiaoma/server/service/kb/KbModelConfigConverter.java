/**
 * service/kb 模块的 KbModelConfigConverter 类/接口定义。
 */
package com.xiaoma.server.service.kb;

import com.xiaoma.server.dto.admin.AdminKbModelConfigRequest;
import com.xiaoma.server.dto.admin.AdminKbModelConfigResponse;
import com.xiaoma.server.entity.kb.KbModelConfig;

/**
 * KbModelConfigConverter 类。
 */
public final class KbModelConfigConverter {

    private KbModelConfigConverter() {
    }

    /**
     * toResponse 方法。
     * @param c 参数说明
     * @return 返回值说明
     */
    public static AdminKbModelConfigResponse toResponse(KbModelConfig c) {
        if (c == null) {
            return null;
        }
        return new AdminKbModelConfigResponse(
                c.getId(),
                c.getName(),
                c.getIsDefault(),
                new AdminKbModelConfigResponse.Milvus(
                        c.getMilvusHost(),
                        c.getMilvusPort(),
                        c.getMilvusUsername(),
                        c.getMilvusPassword(),
                        c.getMilvusDatabase(),
                        c.getMilvusCollection(),
                        c.getMilvusVectorDim(),
                        c.getMilvusMetricType(),
                        c.getMilvusIndexType(),
                        c.getMilvusConsistencyLevel()
                ),
                new AdminKbModelConfigResponse.Embedding(
                        c.getEmbeddingProvider(),
                        c.getEmbeddingModel(),
                        c.getEmbeddingApiKey(),
                        c.getEmbeddingBaseUrl(),
                        c.getEmbeddingQianwenUrl(),
                        c.getEmbeddingOllamaUrl(),
                        c.getEmbeddingOllamaModel()
                ),
                new AdminKbModelConfigResponse.Rerank(
                        c.getRerankEnabled(),
                        c.getRerankProvider(),
                        c.getRerankModel(),
                        c.getRerankApiKey(),
                        c.getRerankBaseUrl(),
                        c.getRerankOllamaUrl(),
                        c.getRerankConcurrency(),
                        c.getRerankTopK(),
                        c.getRerankLimit(),
                        c.getRerankPromptTemplate(),
                        c.getRerankTemperature(),
                        c.getRerankMaxChunksPerDoc()
                ),
                new AdminKbModelConfigResponse.Chat(
                        c.getChatProvider(),
                        c.getChatModel(),
                        c.getChatApiKey(),
                        c.getChatBaseUrl(),
                        c.getChatOllamaUrl(),
                        c.getChatTemperature(),
                        c.getChatMaxTokens()
                ),
                new AdminKbModelConfigResponse.Search(
                        c.getLocalThreshold(),
                        c.getTopK(),
                        c.getDefaultCategoryId(),
                        c.getHistoryRounds(),
                        c.getHistoryMaxChars(),
                        c.getContextThreshold(),
                        c.getContextMaxChars(),
                        c.getAutoWriteEnabled(),
                        c.getAutoWriteThreshold()
                ),
                new AdminKbModelConfigResponse.AuthSession(
                        c.getJwtSecret(),
                        c.getJwtExpireMinutes(),
                        c.getCodeTtlSeconds(),
                        c.getCodeResendIntervalSeconds(),
                        c.getSessionTtlMinutes(),
                        c.getSessionRenewThresholdMinutes()
                ),
                c.getVersion(),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }

    /**
     * fromRequest 方法。
     * @param req 参数说明
     * @return 返回值说明
     */
    public static KbModelConfig fromRequest(AdminKbModelConfigRequest req) {
        if (req == null) {
            return null;
        }
        KbModelConfig c = new KbModelConfig();

        AdminKbModelConfigRequest.Milvus m = req.milvus();
        c.setMilvusHost(m.host());
        c.setMilvusPort(m.port());
        c.setMilvusUsername(m.username());
        c.setMilvusPassword(m.password());
        c.setMilvusDatabase(m.database());
        c.setMilvusCollection(m.collection());
        c.setMilvusVectorDim(m.vectorDim());
        c.setMilvusMetricType(m.metricType());
        c.setMilvusIndexType(m.indexType());
        c.setMilvusConsistencyLevel(m.consistencyLevel());

        AdminKbModelConfigRequest.Embedding e = req.embedding();
        c.setEmbeddingProvider(e.provider());
        c.setEmbeddingModel(e.model());
        c.setEmbeddingApiKey(e.apiKey());
        c.setEmbeddingBaseUrl(e.baseUrl());
        c.setEmbeddingQianwenUrl(e.qianwenUrl());
        c.setEmbeddingOllamaUrl(e.ollamaUrl());
        c.setEmbeddingOllamaModel(e.ollamaModel());

        AdminKbModelConfigRequest.Rerank r = req.rerank();
        c.setRerankEnabled(r.enabled());
        c.setRerankProvider(r.provider());
        c.setRerankModel(r.model());
        c.setRerankApiKey(r.apiKey());
        c.setRerankBaseUrl(r.baseUrl());
        c.setRerankOllamaUrl(r.ollamaUrl());
        c.setRerankConcurrency(r.concurrency());
        c.setRerankTopK(r.topK());
        c.setRerankLimit(r.limit());
        c.setRerankPromptTemplate(r.promptTemplate());
        c.setRerankTemperature(r.temperature());
        c.setRerankMaxChunksPerDoc(r.maxChunksPerDoc());

        AdminKbModelConfigRequest.Chat chat = req.chat();
        c.setChatProvider(chat.provider());
        c.setChatModel(chat.model());
        c.setChatApiKey(chat.apiKey());
        c.setChatBaseUrl(chat.baseUrl());
        c.setChatOllamaUrl(chat.ollamaUrl());
        c.setChatTemperature(chat.temperature());
        c.setChatMaxTokens(chat.maxTokens());

        AdminKbModelConfigRequest.Search s = req.search();
        c.setLocalThreshold(s.localThreshold());
        c.setTopK(s.topK());
        c.setDefaultCategoryId(s.defaultCategoryId());
        c.setHistoryRounds(s.historyRounds());
        c.setHistoryMaxChars(s.historyMaxChars());
        c.setContextThreshold(s.contextThreshold());
        c.setContextMaxChars(s.contextMaxChars());
        c.setAutoWriteEnabled(s.autoWriteEnabled());
        c.setAutoWriteThreshold(s.autoWriteThreshold());

        AdminKbModelConfigRequest.AuthSession a = req.authSession();
        c.setJwtSecret(a.jwtSecret());
        c.setJwtExpireMinutes(a.jwtExpireMinutes());
        c.setCodeTtlSeconds(a.codeTtlSeconds());
        c.setCodeResendIntervalSeconds(a.codeResendIntervalSeconds());
        c.setSessionTtlMinutes(a.sessionTtlMinutes());
        c.setSessionRenewThresholdMinutes(a.sessionRenewThresholdMinutes());

        return c;
    }
}
