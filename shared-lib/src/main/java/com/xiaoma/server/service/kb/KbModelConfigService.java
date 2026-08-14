/**
 * service/kb 模块的 KbModelConfigService 类/接口定义。
 */
package com.xiaoma.server.service.kb;

import com.alibaba.fastjson2.JSON;
import com.xiaoma.server.common.BizException;
import com.xiaoma.server.config.KbProperties;
import com.xiaoma.server.entity.kb.KbCategory;
import com.xiaoma.server.entity.kb.KbModelConfig;
import com.xiaoma.server.mapper.kb.KbCategoryMapper;
import com.xiaoma.server.mapper.kb.KbModelConfigMapper;
import com.xiaoma.server.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 模型配置单点服务。
 * <p>
 * - id=1 的记录为默认配置，用于 Milvus 初始化以及分类未指定配置时的回退。
 * - 每个知识库一级分类可通过 model_config_id 引用独立配置；未引用时使用默认配置。
 * - 配置按 configId 缓存到 Redis，TTL 10 分钟；更新时主动失效。
 */
@Service
/**
 * KbModelConfigService 类。
 */
public class KbModelConfigService {

    private static final Logger log = LoggerFactory.getLogger(KbModelConfigService.class);

    private static final String KEY_DEFAULT = "kb:model:config:default";
    private static final String KEY_CONFIG_PREFIX = "kb:model:config:";
    private static final long CACHE_TTL_MINUTES = 10;

    private final KbModelConfigMapper modelConfigMapper; // modelConfig 数据访问
    private final KbCategoryMapper categoryMapper; // category 数据访问
    private final RedisService redisService; // redis 服务
    private final KbProperties kbProperties; // kb 配置属性
    private final KbSchemaMigration kbSchemaMigration; // kbSchemaMigration 字段
    private final ObjectProvider<RetryableMilvusClient> retryableMilvusClientProvider; // Milvus 客户端包装器提供者

    @Value("${app.jwt.secret}")
    private String jwtSecret; // jwtSecret 字段

    @Value("${app.jwt.expire-minutes}")
    private Integer jwtExpireMinutes; // jwtExpireMinutes 字段

    @Value("${app.code.ttl-seconds:300}")
    private Integer codeTtlSeconds; // codeTtlSeconds 字段

    @Value("${app.code.resend-interval-seconds:60}")
    private Integer codeResendIntervalSeconds; // codeResendIntervalSeconds 字段

    @Value("${app.session.ttl-minutes:120}")
    private Integer sessionTtlMinutes; // sessionTtlMinutes 字段

    @Value("${app.session.renew-threshold-minutes:60}")
    private Integer sessionRenewThresholdMinutes; // sessionRenewThresholdMinutes 字段

    /**
     * 构造 KbModelConfigService 实例。
     */
    public KbModelConfigService(KbModelConfigMapper modelConfigMapper,
                                KbCategoryMapper categoryMapper,
                                RedisService redisService,
                                KbProperties kbProperties,
                                KbSchemaMigration kbSchemaMigration,
                                ObjectProvider<RetryableMilvusClient> retryableMilvusClientProvider) {
        this.modelConfigMapper = modelConfigMapper;
        this.categoryMapper = categoryMapper;
        this.redisService = redisService;
        this.kbProperties = kbProperties;
        this.kbSchemaMigration = kbSchemaMigration;
        this.retryableMilvusClientProvider = retryableMilvusClientProvider;
    }

    /**
     * 获取默认配置（Redis → DB → YAML 兜底）。
     */
    public KbModelConfig current() {
        return current((Integer) null);
    }

    /**
     * 获取指定分类当前应使用的配置。
     * 分类未指定配置时返回默认配置。
     */
    public KbModelConfig current(Integer categoryId) {
        if (categoryId == null) {
            return loadDefault();
        }
        KbCategory category = categoryMapper.selectById(categoryId);
        return current(category);
    }

    /**
     * 根据分类对象解析配置。
     */
    public KbModelConfig current(KbCategory category) {
        if (category == null) {
            return loadDefault();
        }
        Integer configId = category.getModelConfigId();
        if (configId == null) {
            return loadDefault();
        }
        KbModelConfig config = loadById(configId);
        if (config == null) {
            log.warn("分类 {} 引用的模型配置 {} 不存在，回退到默认配置", category.getId(), configId);
            return loadDefault();
        }
        return config;
    }

    /**
     * 直接从 DB 加载默认配置（不走缓存），必要时用 YAML 初始化。
     */
    public KbModelConfig loadDefaultFromDb() {
        KbModelConfig config = modelConfigMapper.selectDefault();
        if (config == null) {
            log.info("数据库中不存在默认模型配置，使用 KbProperties 初始化");
            config = buildFromKbProperties();
            config.setIsDefault(1);
            config.setName("default");
            config.setVersion(1);
            config.setCreatedAt(LocalDateTime.now());
            config.setUpdatedAt(LocalDateTime.now());
            modelConfigMapper.insert(config);
        }
        return config;
    }

    /**
     * 保存默认配置。
     */
    @Transactional
    public void saveDefault(KbModelConfig config) {
        KbModelConfig existing = loadDefaultFromDb();
        config.setId(existing.getId());
        config.setIsDefault(1);
        config.setName(existing.getName());
        config.setVersion(existing.getVersion() == null ? 1 : existing.getVersion() + 1);
        config.setCreatedAt(existing.getCreatedAt());
        config.setUpdatedAt(LocalDateTime.now());
        modelConfigMapper.updateById(config);
        evictDefaultCache();
        reinitializeMilvusClient();
        log.info("默认模型配置已更新，version={}", config.getVersion());
    }

    private void reinitializeMilvusClient() {
        try {
            RetryableMilvusClient client = retryableMilvusClientProvider.getIfAvailable();
            if (client != null) {
                client.reinitialize();
                log.info("Milvus 客户端已重置，将按最新配置重新初始化");
            }
        } catch (Exception e) {
            log.warn("重置 Milvus 客户端失败: {}", e.getMessage());
        }
    }

    /**
     * 为指定分类保存/更新独立配置。
     */
    @Transactional
    public void saveForCategory(Integer categoryId, KbModelConfig config) {
        KbCategory category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BizException("知识库分类不存在");
        }

        Integer existingConfigId = category.getModelConfigId();
        if (existingConfigId != null) {
            config.setId(existingConfigId);
            KbModelConfig existing = modelConfigMapper.selectById(existingConfigId);
            config.setVersion(existing == null || existing.getVersion() == null ? 1 : existing.getVersion() + 1);
            config.setIsDefault(0);
            config.setName(category.getName());
            config.setCreatedAt(existing == null ? LocalDateTime.now() : existing.getCreatedAt());
            config.setUpdatedAt(LocalDateTime.now());
            modelConfigMapper.updateById(config);
            evictConfigCache(existingConfigId);
        } else {
            config.setIsDefault(0);
            config.setName(category.getName());
            config.setVersion(1);
            config.setCreatedAt(LocalDateTime.now());
            config.setUpdatedAt(LocalDateTime.now());
            modelConfigMapper.insert(config);

            category.setModelConfigId(config.getId());
            categoryMapper.updateById(category);
            evictConfigCache(config.getId());
        }
        log.info("分类 {} 的模型配置已更新", categoryId);
    }

    /**
     * 重置分类配置为继承默认配置。
     */
    @Transactional
    public void resetCategory(Integer categoryId) {
        KbCategory category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BizException("知识库分类不存在");
        }
        Integer configId = category.getModelConfigId();
        if (configId != null) {
            category.setModelConfigId(null);
            categoryMapper.updateById(category);
            modelConfigMapper.deleteById(configId);
            evictConfigCache(configId);
            log.info("分类 {} 已重置为默认模型配置，并删除独立配置 {}", categoryId, configId);
        }
    }

    /**
     * 刷新指定配置的缓存。
     */
    public void refreshCache(Integer configId) {
        if (configId == null) {
            evictDefaultCache();
            return;
        }
        evictConfigCache(configId);
    }

    /**
     * 刷新默认配置缓存。
     */
    public void refreshDefaultCache() {
        evictDefaultCache();
    }

    // ---------- private ----------

    private KbModelConfig loadDefault() {
        String cached = redisService.get(KEY_DEFAULT);
        if (cached != null && !cached.isBlank()) {
            try {
                return JSON.parseObject(cached, KbModelConfig.class);
            } catch (Exception e) {
                log.warn("默认模型配置缓存解析失败，重新加载", e);
            }
        }
        KbModelConfig config = loadDefaultFromDb();
        putCache(KEY_DEFAULT, config);
        return config;
    }

    private KbModelConfig loadById(Integer configId) {
        String key = KEY_CONFIG_PREFIX + configId;
        String cached = redisService.get(key);
        if (cached != null && !cached.isBlank()) {
            try {
                return JSON.parseObject(cached, KbModelConfig.class);
            } catch (Exception e) {
                log.warn("模型配置缓存解析失败 configId={}，重新加载", configId, e);
            }
        }
        KbModelConfig config = modelConfigMapper.selectById(configId);
        if (config != null) {
            putCache(key, config);
        }
        return config;
    }

    private void putCache(String key, KbModelConfig config) {
        try {
            redisService.set(key, JSON.toJSONString(config), CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("模型配置写入 Redis 失败: {}", e.getMessage());
        }
    }

    private void evictDefaultCache() {
        redisService.delete(KEY_DEFAULT);
    }

    private void evictConfigCache(Integer configId) {
        redisService.delete(KEY_CONFIG_PREFIX + configId);
    }

    /**
     * 使用当前 YAML 中的 KbProperties 构造默认配置实体。
     */
    private KbModelConfig buildFromKbProperties() {
        KbModelConfig c = new KbModelConfig();

        KbProperties.Milvus m = kbProperties.getMilvus();
        c.setMilvusHost(m.getHost());
        c.setMilvusPort(m.getPort());
        c.setMilvusUsername(m.getUsername());
        c.setMilvusPassword(m.getPassword());
        c.setMilvusDatabase(m.getDatabase());
        c.setMilvusCollection(m.getCollection());
        c.setMilvusVectorDim(m.getVectorDim());
        c.setMilvusMetricType(m.getMetricType());
        c.setMilvusIndexType(m.getIndexType());
        c.setMilvusConsistencyLevel(m.getConsistencyLevel());

        KbProperties.Embedding e = kbProperties.getEmbedding();
        c.setEmbeddingProvider(e.getProvider());
        c.setEmbeddingModel(e.getModel());
        c.setEmbeddingApiKey(e.getApiKey());
        c.setEmbeddingBaseUrl(e.getBaseUrl());
        c.setEmbeddingQianwenUrl("https://dashscope.aliyuncs.com/compatible-mode/v1");
        c.setEmbeddingOllamaUrl(e.getOllamaUrl());
        c.setEmbeddingOllamaModel(e.getOllamaModel());

        KbProperties.Chat chat = kbProperties.getChat();
        c.setRerankEnabled(Boolean.TRUE.equals(chat.getRerankEnabled()) ? 1 : 0);
        c.setRerankProvider("ollama");
        c.setRerankModel("dengcao/Qwen3-Reranker-0.6B:Q8_0");
        c.setRerankApiKey("sk-avfyfwkuldvsrpmlcsmpcvudhxwtysdraiqftbymikzydhyh");
        c.setRerankBaseUrl("https://api.siliconflow.cn/v1");
        c.setRerankOllamaUrl("http://127.0.0.1:11434");
        c.setRerankConcurrency(4);
        c.setRerankTopK(chat.getRerankTopK());
        c.setRerankLimit(chat.getRerankLimit());
        c.setRerankPromptTemplate(chat.getRerankPromptTemplate());
        c.setRerankTemperature(BigDecimal.valueOf(0.0));
        c.setRerankMaxChunksPerDoc(512);

        c.setChatProvider("ollama");
        c.setChatModel("qwen:1.8b");
        c.setChatApiKey("sk-avfyfwkuldvsrpmlcsmpcvudhxwtysdraiqftbymikzydhyh");
        c.setChatBaseUrl("https://api.siliconflow.cn/v1");
        c.setChatOllamaUrl("http://127.0.0.1:11434");
        c.setChatTemperature(BigDecimal.valueOf(0.7));
        c.setChatMaxTokens(2048);

        c.setLocalThreshold(BigDecimal.valueOf(chat.getLocalThreshold()));
        c.setTopK(chat.getTopK());
        c.setDefaultCategoryId(chat.getDefaultCategoryId());
        c.setHistoryRounds(chat.getHistoryRounds());
        c.setHistoryMaxChars(chat.getHistoryMaxChars());
        c.setContextThreshold(BigDecimal.valueOf(chat.getContextThreshold()));
        c.setContextMaxChars(chat.getContextMaxChars());
       /* c.setAutoWriteEnabled(Boolean.TRUE.equals(chat.getAutoWriteEnabled()) ? 1 : 0);
        c.setAutoWriteThreshold(BigDecimal.valueOf(chat.getAutoWriteThreshold()));*/

        c.setJwtSecret(jwtSecret);
        c.setJwtExpireMinutes(jwtExpireMinutes);
        c.setCodeTtlSeconds(codeTtlSeconds);
        c.setCodeResendIntervalSeconds(codeResendIntervalSeconds);
        c.setSessionTtlMinutes(sessionTtlMinutes);
        c.setSessionRenewThresholdMinutes(sessionRenewThresholdMinutes);

        return c;
    }
}
