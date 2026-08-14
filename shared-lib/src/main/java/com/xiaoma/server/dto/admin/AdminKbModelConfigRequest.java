/**
 * dto/admin 模块的 AdminKbModelConfigRequest 类/接口定义。
 */
package com.xiaoma.server.dto.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AdminKbModelConfigRequest(
        @NotNull @Valid Milvus milvus,
        @NotNull @Valid Embedding embedding,
        @NotNull @Valid Rerank rerank,
        @NotNull @Valid Chat chat,
        @NotNull @Valid Search search,
        @NotNull @Valid AuthSession authSession
) {
    /**
     * Milvus 方法。
     * @return 返回值说明
     */
    public record Milvus(
            @NotBlank String host,
            @NotNull Integer port,
            @NotBlank String username,
            @NotBlank String password,
            @NotBlank String database,
            @NotBlank String collection,
            @NotNull Integer vectorDim,
            @NotBlank String metricType,
            @NotBlank String indexType,
            @NotBlank String consistencyLevel
    ) {
    }

    /**
     * Embedding 方法。
     * @return 返回值说明
     */
    public record Embedding(
            @NotBlank String provider,
            @NotBlank String model,
            @NotNull String apiKey,
            @NotBlank String baseUrl,
            @NotBlank String qianwenUrl,
            @NotBlank String ollamaUrl,
            @NotBlank String ollamaModel
    ) {
    }

    /**
     * Rerank 方法。
     * @return 返回值说明
     */
    public record Rerank(
            @NotNull Integer enabled,
            @NotBlank String provider,
            @NotBlank String model,
            @NotNull String apiKey,
            @NotBlank String baseUrl,
            @NotBlank String ollamaUrl,
            @NotNull Integer concurrency,
            @NotNull Integer topK,
            @NotNull Integer limit,
            @NotBlank String promptTemplate,
            @NotNull BigDecimal temperature,
            @NotNull Integer maxChunksPerDoc
    ) {
    }

    /**
     * Chat 方法。
     * @return 返回值说明
     */
    public record Chat(
            @NotBlank String provider,
            @NotBlank String model,
            @NotNull String apiKey,
            @NotBlank String baseUrl,
            @NotBlank String ollamaUrl,
            @NotNull BigDecimal temperature,
            @NotNull Integer maxTokens
    ) {
    }

    /**
     * Search 方法。
     * @return 返回值说明
     */
    public record Search(
            @NotNull BigDecimal localThreshold,
            @NotNull Integer topK,
            @NotNull Integer defaultCategoryId,
            @NotNull Integer historyRounds,
            @NotNull Integer historyMaxChars,
            @NotNull BigDecimal contextThreshold,
            @NotNull Integer contextMaxChars,
            @NotNull Integer autoWriteEnabled,
            @NotNull BigDecimal autoWriteThreshold
    ) {
    }

    /**
     * AuthSession 方法。
     * @return 返回值说明
     */
    public record AuthSession(
            @NotBlank String jwtSecret,
            @NotNull Integer jwtExpireMinutes,
            @NotNull Integer codeTtlSeconds,
            @NotNull Integer codeResendIntervalSeconds,
            @NotNull Integer sessionTtlMinutes,
            @NotNull Integer sessionRenewThresholdMinutes
    ) {
    }
}
