/**
 * entity/kb 模块的 KbModelConfig 类/接口定义。
 */
package com.xiaoma.server.entity.kb;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("kb_model_config")
/**
 * KbModelConfig 类。
 */
public class KbModelConfig {

    @TableId(type = IdType.AUTO)
    private Integer id; // id 字段
    private String name; // name 字段
    private Integer isDefault; // isDefault 字段

    // 1. Milvus
    private String milvusHost; // milvusHost 字段
    private Integer milvusPort; // milvusPort 字段
    private String milvusUsername; // milvusUsername 字段
    private String milvusPassword; // milvusPassword 字段
    private String milvusDatabase; // milvusDatabase 字段
    private String milvusCollection; // milvusCollection 字段
    private Integer milvusVectorDim; // milvusVectorDim 字段
    private String milvusMetricType; // milvusMetricType 字段
    private String milvusIndexType; // milvusIndexType 字段
    private String milvusConsistencyLevel; // milvusConsistencyLevel 字段

    // 2. Embedding
    private String embeddingProvider; // embeddingProvider 字段
    private String embeddingModel; // embeddingModel 字段
    private String embeddingApiKey; // embeddingApiKey 字段
    private String embeddingBaseUrl; // embeddingBaseUrl 字段
    private String embeddingQianwenUrl; // embeddingQianwenUrl 字段
    private String embeddingOllamaUrl; // embeddingOllamaUrl 字段
    private String embeddingOllamaModel; // embeddingOllamaModel 字段

    // 3. Rerank
    private Integer rerankEnabled; // rerankEnabled 字段
    private String rerankProvider; // rerankProvider 字段
    private String rerankModel; // rerankModel 字段
    private String rerankApiKey; // rerankApiKey 字段
    private String rerankBaseUrl; // rerankBaseUrl 字段
    private String rerankOllamaUrl; // rerankOllamaUrl 字段
    private Integer rerankConcurrency; // rerankConcurrency 字段
    private Integer rerankTopK; // rerankTopK 字段
    private Integer rerankLimit; // rerankLimit 字段
    private String rerankPromptTemplate; // rerankPromptTemplate 字段
    private BigDecimal rerankTemperature; // rerankTemperature 字段
    private Integer rerankMaxChunksPerDoc; // rerankMaxChunksPerDoc 字段

    // 4. Chat
    private String chatProvider; // chatProvider 字段
    private String chatModel; // chatModel 字段
    private String chatApiKey; // chatApiKey 字段
    private String chatBaseUrl; // chatBaseUrl 字段
    private String chatOllamaUrl; // chatOllamaUrl 字段
    private BigDecimal chatTemperature; // chatTemperature 字段
    private Integer chatMaxTokens; // chatMaxTokens 字段

    // 5. Search / chat behavior
    private BigDecimal localThreshold; // localThreshold 字段
    private Integer topK; // topK 字段
    private Integer defaultCategoryId; // defaultCategoryId 字段
    private Integer historyRounds; // historyRounds 字段
    private Integer historyMaxChars; // historyMaxChars 字段
    private BigDecimal contextThreshold; // contextThreshold 字段
    private Integer contextMaxChars; // contextMaxChars 字段
    private Integer autoWriteEnabled; // autoWriteEnabled 字段
    private BigDecimal autoWriteThreshold; // autoWriteThreshold 字段

    // 6. Auth / session / token
    private String jwtSecret; // jwtSecret 字段
    private Integer jwtExpireMinutes; // jwtExpireMinutes 字段
    private Integer codeTtlSeconds; // codeTtlSeconds 字段
    private Integer codeResendIntervalSeconds; // codeResendIntervalSeconds 字段
    private Integer sessionTtlMinutes; // sessionTtlMinutes 字段
    private Integer sessionRenewThresholdMinutes; // sessionRenewThresholdMinutes 字段

    private Integer version; // version 字段
    private LocalDateTime createdAt; // createdAt 字段
    private LocalDateTime updatedAt; // updatedAt 字段

    /**
     * 获取 Id。
     * @return 返回值说明
     */
    public Integer getId() {
        return id;
    }

    /**
     * 设置 Id。
     * @param id 参数说明
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 获取 Name。
     * @return 返回值说明
     */
    public String getName() {
        return name;
    }

    /**
     * 设置 Name。
     * @param name 参数说明
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取 IsDefault。
     * @return 返回值说明
     */
    public Integer getIsDefault() {
        return isDefault;
    }

    /**
     * 设置 IsDefault。
     * @param isDefault 参数说明
     */
    public void setIsDefault(Integer isDefault) {
        this.isDefault = isDefault;
    }

    /**
     * 获取 MilvusHost。
     * @return 返回值说明
     */
    public String getMilvusHost() {
        return milvusHost;
    }

    /**
     * 设置 MilvusHost。
     * @param milvusHost 参数说明
     */
    public void setMilvusHost(String milvusHost) {
        this.milvusHost = milvusHost;
    }

    /**
     * 获取 MilvusPort。
     * @return 返回值说明
     */
    public Integer getMilvusPort() {
        return milvusPort;
    }

    /**
     * 设置 MilvusPort。
     * @param milvusPort 参数说明
     */
    public void setMilvusPort(Integer milvusPort) {
        this.milvusPort = milvusPort;
    }

    /**
     * 获取 MilvusUsername。
     * @return 返回值说明
     */
    public String getMilvusUsername() {
        return milvusUsername;
    }

    /**
     * 设置 MilvusUsername。
     * @param milvusUsername 参数说明
     */
    public void setMilvusUsername(String milvusUsername) {
        this.milvusUsername = milvusUsername;
    }

    /**
     * 获取 MilvusPassword。
     * @return 返回值说明
     */
    public String getMilvusPassword() {
        return milvusPassword;
    }

    /**
     * 设置 MilvusPassword。
     * @param milvusPassword 参数说明
     */
    public void setMilvusPassword(String milvusPassword) {
        this.milvusPassword = milvusPassword;
    }

    /**
     * 获取 MilvusDatabase。
     * @return 返回值说明
     */
    public String getMilvusDatabase() {
        return milvusDatabase;
    }

    /**
     * 设置 MilvusDatabase。
     * @param milvusDatabase 参数说明
     */
    public void setMilvusDatabase(String milvusDatabase) {
        this.milvusDatabase = milvusDatabase;
    }

    /**
     * 获取 MilvusCollection。
     * @return 返回值说明
     */
    public String getMilvusCollection() {
        return milvusCollection;
    }

    /**
     * 设置 MilvusCollection。
     * @param milvusCollection 参数说明
     */
    public void setMilvusCollection(String milvusCollection) {
        this.milvusCollection = milvusCollection;
    }

    /**
     * 获取 MilvusVectorDim。
     * @return 返回值说明
     */
    public Integer getMilvusVectorDim() {
        return milvusVectorDim;
    }

    /**
     * 设置 MilvusVectorDim。
     * @param milvusVectorDim 参数说明
     */
    public void setMilvusVectorDim(Integer milvusVectorDim) {
        this.milvusVectorDim = milvusVectorDim;
    }

    /**
     * 获取 MilvusMetricType。
     * @return 返回值说明
     */
    public String getMilvusMetricType() {
        return milvusMetricType;
    }

    /**
     * 设置 MilvusMetricType。
     * @param milvusMetricType 参数说明
     */
    public void setMilvusMetricType(String milvusMetricType) {
        this.milvusMetricType = milvusMetricType;
    }

    /**
     * 获取 MilvusIndexType。
     * @return 返回值说明
     */
    public String getMilvusIndexType() {
        return milvusIndexType;
    }

    /**
     * 设置 MilvusIndexType。
     * @param milvusIndexType 参数说明
     */
    public void setMilvusIndexType(String milvusIndexType) {
        this.milvusIndexType = milvusIndexType;
    }

    /**
     * 获取 MilvusConsistencyLevel。
     * @return 返回值说明
     */
    public String getMilvusConsistencyLevel() {
        return milvusConsistencyLevel;
    }

    /**
     * 设置 MilvusConsistencyLevel。
     * @param milvusConsistencyLevel 参数说明
     */
    public void setMilvusConsistencyLevel(String milvusConsistencyLevel) {
        this.milvusConsistencyLevel = milvusConsistencyLevel;
    }

    /**
     * 获取 EmbeddingProvider。
     * @return 返回值说明
     */
    public String getEmbeddingProvider() {
        return embeddingProvider;
    }

    /**
     * 设置 EmbeddingProvider。
     * @param embeddingProvider 参数说明
     */
    public void setEmbeddingProvider(String embeddingProvider) {
        this.embeddingProvider = embeddingProvider;
    }

    /**
     * 获取 EmbeddingModel。
     * @return 返回值说明
     */
    public String getEmbeddingModel() {
        return embeddingModel;
    }

    /**
     * 设置 EmbeddingModel。
     * @param embeddingModel 参数说明
     */
    public void setEmbeddingModel(String embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    /**
     * 获取 EmbeddingApiKey。
     * @return 返回值说明
     */
    public String getEmbeddingApiKey() {
        return embeddingApiKey;
    }

    /**
     * 设置 EmbeddingApiKey。
     * @param embeddingApiKey 参数说明
     */
    public void setEmbeddingApiKey(String embeddingApiKey) {
        this.embeddingApiKey = embeddingApiKey;
    }

    /**
     * 获取 EmbeddingBaseUrl。
     * @return 返回值说明
     */
    public String getEmbeddingBaseUrl() {
        return embeddingBaseUrl;
    }

    /**
     * 设置 EmbeddingBaseUrl。
     * @param embeddingBaseUrl 参数说明
     */
    public void setEmbeddingBaseUrl(String embeddingBaseUrl) {
        this.embeddingBaseUrl = embeddingBaseUrl;
    }

    /**
     * 获取 EmbeddingQianwenUrl。
     * @return 返回值说明
     */
    public String getEmbeddingQianwenUrl() {
        return embeddingQianwenUrl;
    }

    /**
     * 设置 EmbeddingQianwenUrl。
     * @param embeddingQianwenUrl 参数说明
     */
    public void setEmbeddingQianwenUrl(String embeddingQianwenUrl) {
        this.embeddingQianwenUrl = embeddingQianwenUrl;
    }

    /**
     * 获取 EmbeddingOllamaUrl。
     * @return 返回值说明
     */
    public String getEmbeddingOllamaUrl() {
        return embeddingOllamaUrl;
    }

    /**
     * 设置 EmbeddingOllamaUrl。
     * @param embeddingOllamaUrl 参数说明
     */
    public void setEmbeddingOllamaUrl(String embeddingOllamaUrl) {
        this.embeddingOllamaUrl = embeddingOllamaUrl;
    }

    /**
     * 获取 EmbeddingOllamaModel。
     * @return 返回值说明
     */
    public String getEmbeddingOllamaModel() {
        return embeddingOllamaModel;
    }

    /**
     * 设置 EmbeddingOllamaModel。
     * @param embeddingOllamaModel 参数说明
     */
    public void setEmbeddingOllamaModel(String embeddingOllamaModel) {
        this.embeddingOllamaModel = embeddingOllamaModel;
    }

    /**
     * 获取 RerankEnabled。
     * @return 返回值说明
     */
    public Integer getRerankEnabled() {
        return rerankEnabled;
    }

    /**
     * 设置 RerankEnabled。
     * @param rerankEnabled 参数说明
     */
    public void setRerankEnabled(Integer rerankEnabled) {
        this.rerankEnabled = rerankEnabled;
    }

    /**
     * 获取 RerankProvider。
     * @return 返回值说明
     */
    public String getRerankProvider() {
        return rerankProvider;
    }

    /**
     * 设置 RerankProvider。
     * @param rerankProvider 参数说明
     */
    public void setRerankProvider(String rerankProvider) {
        this.rerankProvider = rerankProvider;
    }

    /**
     * 获取 RerankModel。
     * @return 返回值说明
     */
    public String getRerankModel() {
        return rerankModel;
    }

    /**
     * 设置 RerankModel。
     * @param rerankModel 参数说明
     */
    public void setRerankModel(String rerankModel) {
        this.rerankModel = rerankModel;
    }

    /**
     * 获取 RerankApiKey。
     * @return 返回值说明
     */
    public String getRerankApiKey() {
        return rerankApiKey;
    }

    /**
     * 设置 RerankApiKey。
     * @param rerankApiKey 参数说明
     */
    public void setRerankApiKey(String rerankApiKey) {
        this.rerankApiKey = rerankApiKey;
    }

    /**
     * 获取 RerankBaseUrl。
     * @return 返回值说明
     */
    public String getRerankBaseUrl() {
        return rerankBaseUrl;
    }

    /**
     * 设置 RerankBaseUrl。
     * @param rerankBaseUrl 参数说明
     */
    public void setRerankBaseUrl(String rerankBaseUrl) {
        this.rerankBaseUrl = rerankBaseUrl;
    }

    /**
     * 获取 RerankOllamaUrl。
     * @return 返回值说明
     */
    public String getRerankOllamaUrl() {
        return rerankOllamaUrl;
    }

    /**
     * 设置 RerankOllamaUrl。
     * @param rerankOllamaUrl 参数说明
     */
    public void setRerankOllamaUrl(String rerankOllamaUrl) {
        this.rerankOllamaUrl = rerankOllamaUrl;
    }

    /**
     * 获取 RerankConcurrency。
     * @return 返回值说明
     */
    public Integer getRerankConcurrency() {
        return rerankConcurrency;
    }

    /**
     * 设置 RerankConcurrency。
     * @param rerankConcurrency 参数说明
     */
    public void setRerankConcurrency(Integer rerankConcurrency) {
        this.rerankConcurrency = rerankConcurrency;
    }

    /**
     * 获取 RerankTopK。
     * @return 返回值说明
     */
    public Integer getRerankTopK() {
        return rerankTopK;
    }

    /**
     * 设置 RerankTopK。
     * @param rerankTopK 参数说明
     */
    public void setRerankTopK(Integer rerankTopK) {
        this.rerankTopK = rerankTopK;
    }

    /**
     * 获取 RerankLimit。
     * @return 返回值说明
     */
    public Integer getRerankLimit() {
        return rerankLimit;
    }

    /**
     * 设置 RerankLimit。
     * @param rerankLimit 参数说明
     */
    public void setRerankLimit(Integer rerankLimit) {
        this.rerankLimit = rerankLimit;
    }

    /**
     * 获取 RerankPromptTemplate。
     * @return 返回值说明
     */
    public String getRerankPromptTemplate() {
        return rerankPromptTemplate;
    }

    /**
     * 设置 RerankPromptTemplate。
     * @param rerankPromptTemplate 参数说明
     */
    public void setRerankPromptTemplate(String rerankPromptTemplate) {
        this.rerankPromptTemplate = rerankPromptTemplate;
    }

    /**
     * 获取 RerankTemperature。
     * @return 返回值说明
     */
    public BigDecimal getRerankTemperature() {
        return rerankTemperature;
    }

    /**
     * 设置 RerankTemperature。
     * @param rerankTemperature 参数说明
     */
    public void setRerankTemperature(BigDecimal rerankTemperature) {
        this.rerankTemperature = rerankTemperature;
    }

    /**
     * 获取 RerankMaxChunksPerDoc。
     * @return 返回值说明
     */
    public Integer getRerankMaxChunksPerDoc() {
        return rerankMaxChunksPerDoc;
    }

    /**
     * 设置 RerankMaxChunksPerDoc。
     * @param rerankMaxChunksPerDoc 参数说明
     */
    public void setRerankMaxChunksPerDoc(Integer rerankMaxChunksPerDoc) {
        this.rerankMaxChunksPerDoc = rerankMaxChunksPerDoc;
    }

    /**
     * 获取 ChatProvider。
     * @return 返回值说明
     */
    public String getChatProvider() {
        return chatProvider;
    }

    /**
     * 设置 ChatProvider。
     * @param chatProvider 参数说明
     */
    public void setChatProvider(String chatProvider) {
        this.chatProvider = chatProvider;
    }

    /**
     * 获取 ChatModel。
     * @return 返回值说明
     */
    public String getChatModel() {
        return chatModel;
    }

    /**
     * 设置 ChatModel。
     * @param chatModel 参数说明
     */
    public void setChatModel(String chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * 获取 ChatApiKey。
     * @return 返回值说明
     */
    public String getChatApiKey() {
        return chatApiKey;
    }

    /**
     * 设置 ChatApiKey。
     * @param chatApiKey 参数说明
     */
    public void setChatApiKey(String chatApiKey) {
        this.chatApiKey = chatApiKey;
    }

    /**
     * 获取 ChatBaseUrl。
     * @return 返回值说明
     */
    public String getChatBaseUrl() {
        return chatBaseUrl;
    }

    /**
     * 设置 ChatBaseUrl。
     * @param chatBaseUrl 参数说明
     */
    public void setChatBaseUrl(String chatBaseUrl) {
        this.chatBaseUrl = chatBaseUrl;
    }

    /**
     * 获取 ChatOllamaUrl。
     * @return 返回值说明
     */
    public String getChatOllamaUrl() {
        return chatOllamaUrl;
    }

    /**
     * 设置 ChatOllamaUrl。
     * @param chatOllamaUrl 参数说明
     */
    public void setChatOllamaUrl(String chatOllamaUrl) {
        this.chatOllamaUrl = chatOllamaUrl;
    }

    /**
     * 获取 ChatTemperature。
     * @return 返回值说明
     */
    public BigDecimal getChatTemperature() {
        return chatTemperature;
    }

    /**
     * 设置 ChatTemperature。
     * @param chatTemperature 参数说明
     */
    public void setChatTemperature(BigDecimal chatTemperature) {
        this.chatTemperature = chatTemperature;
    }

    /**
     * 获取 ChatMaxTokens。
     * @return 返回值说明
     */
    public Integer getChatMaxTokens() {
        return chatMaxTokens;
    }

    /**
     * 设置 ChatMaxTokens。
     * @param chatMaxTokens 参数说明
     */
    public void setChatMaxTokens(Integer chatMaxTokens) {
        this.chatMaxTokens = chatMaxTokens;
    }

    /**
     * 获取 LocalThreshold。
     * @return 返回值说明
     */
    public BigDecimal getLocalThreshold() {
        return localThreshold;
    }

    /**
     * 设置 LocalThreshold。
     * @param localThreshold 参数说明
     */
    public void setLocalThreshold(BigDecimal localThreshold) {
        this.localThreshold = localThreshold;
    }

    /**
     * 获取 TopK。
     * @return 返回值说明
     */
    public Integer getTopK() {
        return topK;
    }

    /**
     * 设置 TopK。
     * @param topK 参数说明
     */
    public void setTopK(Integer topK) {
        this.topK = topK;
    }

    /**
     * 获取 DefaultCategoryId。
     * @return 返回值说明
     */
    public Integer getDefaultCategoryId() {
        return defaultCategoryId;
    }

    /**
     * 设置 DefaultCategoryId。
     * @param defaultCategoryId 参数说明
     */
    public void setDefaultCategoryId(Integer defaultCategoryId) {
        this.defaultCategoryId = defaultCategoryId;
    }

    /**
     * 获取 HistoryRounds。
     * @return 返回值说明
     */
    public Integer getHistoryRounds() {
        return historyRounds;
    }

    /**
     * 设置 HistoryRounds。
     * @param historyRounds 参数说明
     */
    public void setHistoryRounds(Integer historyRounds) {
        this.historyRounds = historyRounds;
    }

    /**
     * 获取 HistoryMaxChars。
     * @return 返回值说明
     */
    public Integer getHistoryMaxChars() {
        return historyMaxChars;
    }

    /**
     * 设置 HistoryMaxChars。
     * @param historyMaxChars 参数说明
     */
    public void setHistoryMaxChars(Integer historyMaxChars) {
        this.historyMaxChars = historyMaxChars;
    }

    /**
     * 获取 ContextThreshold。
     * @return 返回值说明
     */
    public BigDecimal getContextThreshold() {
        return contextThreshold;
    }

    /**
     * 设置 ContextThreshold。
     * @param contextThreshold 参数说明
     */
    public void setContextThreshold(BigDecimal contextThreshold) {
        this.contextThreshold = contextThreshold;
    }

    /**
     * 获取 ContextMaxChars。
     * @return 返回值说明
     */
    public Integer getContextMaxChars() {
        return contextMaxChars;
    }

    /**
     * 设置 ContextMaxChars。
     * @param contextMaxChars 参数说明
     */
    public void setContextMaxChars(Integer contextMaxChars) {
        this.contextMaxChars = contextMaxChars;
    }

    /**
     * 获取 AutoWriteEnabled。
     * @return 返回值说明
     */
    public Integer getAutoWriteEnabled() {
        return autoWriteEnabled;
    }

    /**
     * 设置 AutoWriteEnabled。
     * @param autoWriteEnabled 参数说明
     */
    public void setAutoWriteEnabled(Integer autoWriteEnabled) {
        this.autoWriteEnabled = autoWriteEnabled;
    }

    /**
     * 获取 AutoWriteThreshold。
     * @return 返回值说明
     */
    public BigDecimal getAutoWriteThreshold() {
        return autoWriteThreshold;
    }

    /**
     * 设置 AutoWriteThreshold。
     * @param autoWriteThreshold 参数说明
     */
    public void setAutoWriteThreshold(BigDecimal autoWriteThreshold) {
        this.autoWriteThreshold = autoWriteThreshold;
    }

    /**
     * 获取 JwtSecret。
     * @return 返回值说明
     */
    public String getJwtSecret() {
        return jwtSecret;
    }

    /**
     * 设置 JwtSecret。
     * @param jwtSecret 参数说明
     */
    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    /**
     * 获取 JwtExpireMinutes。
     * @return 返回值说明
     */
    public Integer getJwtExpireMinutes() {
        return jwtExpireMinutes;
    }

    /**
     * 设置 JwtExpireMinutes。
     * @param jwtExpireMinutes 参数说明
     */
    public void setJwtExpireMinutes(Integer jwtExpireMinutes) {
        this.jwtExpireMinutes = jwtExpireMinutes;
    }

    /**
     * 获取 CodeTtlSeconds。
     * @return 返回值说明
     */
    public Integer getCodeTtlSeconds() {
        return codeTtlSeconds;
    }

    /**
     * 设置 CodeTtlSeconds。
     * @param codeTtlSeconds 参数说明
     */
    public void setCodeTtlSeconds(Integer codeTtlSeconds) {
        this.codeTtlSeconds = codeTtlSeconds;
    }

    /**
     * 获取 CodeResendIntervalSeconds。
     * @return 返回值说明
     */
    public Integer getCodeResendIntervalSeconds() {
        return codeResendIntervalSeconds;
    }

    /**
     * 设置 CodeResendIntervalSeconds。
     * @param codeResendIntervalSeconds 参数说明
     */
    public void setCodeResendIntervalSeconds(Integer codeResendIntervalSeconds) {
        this.codeResendIntervalSeconds = codeResendIntervalSeconds;
    }

    /**
     * 获取 SessionTtlMinutes。
     * @return 返回值说明
     */
    public Integer getSessionTtlMinutes() {
        return sessionTtlMinutes;
    }

    /**
     * 设置 SessionTtlMinutes。
     * @param sessionTtlMinutes 参数说明
     */
    public void setSessionTtlMinutes(Integer sessionTtlMinutes) {
        this.sessionTtlMinutes = sessionTtlMinutes;
    }

    /**
     * 获取 SessionRenewThresholdMinutes。
     * @return 返回值说明
     */
    public Integer getSessionRenewThresholdMinutes() {
        return sessionRenewThresholdMinutes;
    }

    /**
     * 设置 SessionRenewThresholdMinutes。
     * @param sessionRenewThresholdMinutes 参数说明
     */
    public void setSessionRenewThresholdMinutes(Integer sessionRenewThresholdMinutes) {
        this.sessionRenewThresholdMinutes = sessionRenewThresholdMinutes;
    }

    /**
     * 获取 Version。
     * @return 返回值说明
     */
    public Integer getVersion() {
        return version;
    }

    /**
     * 设置 Version。
     * @param version 参数说明
     */
    public void setVersion(Integer version) {
        this.version = version;
    }

    /**
     * 获取 CreatedAt。
     * @return 返回值说明
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 设置 CreatedAt。
     * @param createdAt 参数说明
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 获取 UpdatedAt。
     * @return 返回值说明
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 设置 UpdatedAt。
     * @param updatedAt 参数说明
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
