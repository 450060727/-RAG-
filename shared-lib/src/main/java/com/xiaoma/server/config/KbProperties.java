/**
 * 知识库配置属性类。
 */
package com.xiaoma.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 知识库配置属性类。
 * 本类定义了 KbProperties 的公共契约与数据结构。
 */
@Component
@ConfigurationProperties(prefix = "kb")
public class KbProperties {

    private Milvus milvus = new Milvus();
    private Embedding embedding = new Embedding();
    private Upload upload = new Upload();
    private Chat chat = new Chat();

    /**
     * 获取 Milvus。
     * @return 返回值说明
     */
    public Milvus getMilvus() {
        return milvus;
    }

    /**
     * 设置 Milvus。
     * @param milvus 参数说明
     */
    public void setMilvus(Milvus milvus) {
        this.milvus = milvus;
    }

    /**
     * 获取 Embedding。
     * @return 返回值说明
     */
    public Embedding getEmbedding() {
        return embedding;
    }

    /**
     * 设置 Embedding。
     * @param embedding 参数说明
     */
    public void setEmbedding(Embedding embedding) {
        this.embedding = embedding;
    }

    /**
     * 获取 Upload。
     * @return 返回值说明
     */
    public Upload getUpload() {
        return upload;
    }

    /**
     * 设置 Upload。
     * @param upload 参数说明
     */
    public void setUpload(Upload upload) {
        this.upload = upload;
    }

    /**
     * 获取 Chat。
     * @return 返回值说明
     */
    public Chat getChat() {
        return chat;
    }

    /**
     * 设置 Chat。
     * @param chat 参数说明
     */
    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public static class Milvus {
        private boolean enabled = true;
        private String host = "localhost";
        private Integer port = 19530;
        private String username = "root";
        private String password = "Milvus";
        private String database = "default";
        private String collection = "knowledge_segment";
        private Integer vectorDim = 1024;
        private String metricType = "COSINE";
        private String indexType = "HNSW";
        private String consistencyLevel = "Bounded";
        private Integer retryCooldownMs = 30000;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getDatabase() {
            return database;
        }

        public void setDatabase(String database) {
            this.database = database;
        }

        public String getCollection() {
            return collection;
        }

        public void setCollection(String collection) {
            this.collection = collection;
        }

        public Integer getVectorDim() {
            return vectorDim;
        }

        public void setVectorDim(Integer vectorDim) {
            this.vectorDim = vectorDim;
        }

        public String getMetricType() {
            return metricType;
        }

        public void setMetricType(String metricType) {
            this.metricType = metricType;
        }

        public String getIndexType() {
            return indexType;
        }

        public void setIndexType(String indexType) {
            this.indexType = indexType;
        }

        public String getConsistencyLevel() {
            return consistencyLevel;
        }

        public void setConsistencyLevel(String consistencyLevel) {
            this.consistencyLevel = consistencyLevel;
        }

        public Integer getRetryCooldownMs() {
            return retryCooldownMs;
        }

        public void setRetryCooldownMs(Integer retryCooldownMs) {
            this.retryCooldownMs = retryCooldownMs;
        }
    }

    public static class Embedding {
        private String provider = "ollama";
        private String model = "BAAI/bge-m3";
        private String apiKey = "";
        private String baseUrl = "https://api.siliconflow.cn/v1";
        private String ollamaUrl = "http://localhost:11434/api/embed";
        private String ollamaModel = "qwen3-embedding:0.6b-q8_0";

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getOllamaUrl() {
            return ollamaUrl;
        }

        public void setOllamaUrl(String ollamaUrl) {
            this.ollamaUrl = ollamaUrl;
        }

        public String getOllamaModel() {
            return ollamaModel;
        }

        public void setOllamaModel(String ollamaModel) {
            this.ollamaModel = ollamaModel;
        }
    }

    public static class Upload {
        private String path = "./uploads/knowledge";
        private String maxFileSize = "100MB";
        private String maxRequestSize = "200MB";
        private List<String> allowedMimeTypes = List.of(
                "text/plain", "text/markdown",
                "application/pdf", "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "image/jpeg", "image/png", "image/webp",
                "audio/mpeg", "audio/wav", "audio/mp4",
                "video/mp4", "video/x-matroska"
        );

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getMaxFileSize() {
            return maxFileSize;
        }

        public void setMaxFileSize(String maxFileSize) {
            this.maxFileSize = maxFileSize;
        }

        public String getMaxRequestSize() {
            return maxRequestSize;
        }

        public void setMaxRequestSize(String maxRequestSize) {
            this.maxRequestSize = maxRequestSize;
        }

        public List<String> getAllowedMimeTypes() {
            return allowedMimeTypes;
        }

        public void setAllowedMimeTypes(List<String> allowedMimeTypes) {
            this.allowedMimeTypes = allowedMimeTypes;
        }
    }

    public static class Chat {
        private Double localThreshold = 0.75;
        private Integer topK = 5;
        private Integer defaultCategoryId = 1;
        private Integer historyRounds = 3;
        private Integer historyMaxChars = 3000;
        private Double contextThreshold = 0.55;
        private Integer contextMaxChars = 4000;
        private Boolean rerankEnabled = true;
        private Integer rerankTopK = 5;
        private Integer rerankLimit = 5;
        private String rerankPromptTemplate = "Given the following query and passage, determine whether the passage is relevant to the query. Output only a floating point number between 0 and 1, where 1 means highly relevant.\n\nQuery: {query}\nPassage: {passage}\n\nRelevance score:";

        public Double getLocalThreshold() {
            return localThreshold;
        }

        public void setLocalThreshold(Double localThreshold) {
            this.localThreshold = localThreshold;
        }

        public Integer getTopK() {
            return topK;
        }

        public void setTopK(Integer topK) {
            this.topK = topK;
        }

        public Integer getDefaultCategoryId() {
            return defaultCategoryId;
        }

        public void setDefaultCategoryId(Integer defaultCategoryId) {
            this.defaultCategoryId = defaultCategoryId;
        }

        public Integer getHistoryRounds() {
            return historyRounds;
        }

        public void setHistoryRounds(Integer historyRounds) {
            this.historyRounds = historyRounds;
        }

        public Integer getHistoryMaxChars() {
            return historyMaxChars;
        }

        public void setHistoryMaxChars(Integer historyMaxChars) {
            this.historyMaxChars = historyMaxChars;
        }

        public Double getContextThreshold() {
            return contextThreshold;
        }

        public void setContextThreshold(Double contextThreshold) {
            this.contextThreshold = contextThreshold;
        }

        public Integer getContextMaxChars() {
            return contextMaxChars;
        }

        public void setContextMaxChars(Integer contextMaxChars) {
            this.contextMaxChars = contextMaxChars;
        }

        public Boolean getRerankEnabled() {
            return rerankEnabled;
        }

        public void setRerankEnabled(Boolean rerankEnabled) {
            this.rerankEnabled = rerankEnabled;
        }

        public Integer getRerankTopK() {
            return rerankTopK;
        }

        public void setRerankTopK(Integer rerankTopK) {
            this.rerankTopK = rerankTopK;
        }

        public Integer getRerankLimit() {
            return rerankLimit;
        }

        public void setRerankLimit(Integer rerankLimit) {
            this.rerankLimit = rerankLimit;
        }

        public String getRerankPromptTemplate() {
            return rerankPromptTemplate;
        }

        public void setRerankPromptTemplate(String rerankPromptTemplate) {
            this.rerankPromptTemplate = rerankPromptTemplate;
        }
    }
}
