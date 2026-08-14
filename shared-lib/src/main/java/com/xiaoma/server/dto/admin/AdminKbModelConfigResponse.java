/**
 * dto/admin 模块的 AdminKbModelConfigResponse 类/接口定义。
 */
package com.xiaoma.server.dto.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminKbModelConfigResponse(
        Integer id,
        String name,
        Integer isDefault,
        Milvus milvus,
        Embedding embedding,
        Rerank rerank,
        Chat chat,
        Search search,
        AuthSession authSession,
        Integer version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * Milvus 方法。
     * @return 返回值说明
     */
    public record Milvus(
            String host,
            Integer port,
            String username,
            String password,
            String database,
            String collection,
            Integer vectorDim,
            String metricType,
            String indexType,
            String consistencyLevel
    ) {
    }

    /**
     * Embedding 方法。
     * @return 返回值说明
     */
    public record Embedding(
            String provider,
            String model,
            String apiKey,
            String baseUrl,
            String qianwenUrl,
            String ollamaUrl,
            String ollamaModel
    ) {
    }

    /**
     * Rerank 方法。
     * @return 返回值说明
     */
    public record Rerank(
            Integer enabled,
            String provider,
            String model,
            String apiKey,
            String baseUrl,
            String ollamaUrl,
            Integer concurrency,
            Integer topK,
            Integer limit,
            String promptTemplate,
            BigDecimal temperature,
            Integer maxChunksPerDoc
    ) {
    }

    /**
     * Chat 方法。
     * @return 返回值说明
     */
    public record Chat(
            String provider,
            String model,
            String apiKey,
            String baseUrl,
            String ollamaUrl,
            BigDecimal temperature,
            Integer maxTokens
    ) {
    }

    /**
     * Search 方法。
     * @return 返回值说明
     */
    public record Search(
            BigDecimal localThreshold,
            Integer topK,
            Integer defaultCategoryId,
            Integer historyRounds,
            Integer historyMaxChars,
            BigDecimal contextThreshold,
            Integer contextMaxChars,
            Integer autoWriteEnabled,
            BigDecimal autoWriteThreshold
    ) {
    }

    /**
     * AuthSession 方法。
     * @return 返回值说明
     */
    public record AuthSession(
            String jwtSecret,
            Integer jwtExpireMinutes,
            Integer codeTtlSeconds,
            Integer codeResendIntervalSeconds,
            Integer sessionTtlMinutes,
            Integer sessionRenewThresholdMinutes
    ) {
    }
}
