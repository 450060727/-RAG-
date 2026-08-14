/**
 * service/kb 模块的 KbSchemaMigration 类/接口定义。
 */
package com.xiaoma.server.service.kb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;

/**
 * 知识库模块运行时 Schema 迁移。
 * <p>
 * 用于在无法使用 schema.sql ALTER TABLE 的老版本 MySQL 上自动补齐列。
 * 幂等：重复执行无副作用。
 */
@Component
/**
 * KbSchemaMigration 类。
 */
public class KbSchemaMigration {

    private static final Logger log = LoggerFactory.getLogger(KbSchemaMigration.class);

    private final JdbcTemplate jdbcTemplate; // jdbcTemplate 字段

    /**
     * 构造 KbSchemaMigration 实例。
     * @param dataSource 参数说明
     */
    public KbSchemaMigration(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * migrate 方法。
     */
    @PostConstruct
    public void migrate() {
        addKbCategoryModelConfigId();
        addKbModelConfigEmbeddingQianwenUrl();
        addKbModelConfigRerankOllamaUrl();
        addKbModelConfigRerankMaxChunksPerDoc();
        addKbModelConfigAutoWrite();
        dropKbCategoryUnusedColumns();
        dropKbModelConfigWrongUniqueKey();
        dropKbModelConfigMilvusEnabled();
    }

    private void addKbCategoryModelConfigId() {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.columns " +
                    "WHERE table_schema = DATABASE() AND table_name = 'kb_category' AND column_name = 'model_config_id'",
                    Integer.class);
            if (count != null && count > 0) {
                return;
            }
            jdbcTemplate.execute("ALTER TABLE `kb_category` " +
                    "ADD COLUMN `model_config_id` INT NULL COMMENT '引用的 kb_model_config.id，NULL 表示使用默认配置', " +
                    "ADD INDEX `idx_model_config_id` (`model_config_id`)");
            log.info("已自动为 kb_category 表添加 model_config_id 列");
        } catch (Exception e) {
            log.warn("自动添加 kb_category.model_config_id 列失败: {}", e.getMessage());
        }
    }

    private void addKbModelConfigEmbeddingQianwenUrl() {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.columns " +
                    "WHERE table_schema = DATABASE() AND table_name = 'kb_model_config' AND column_name = 'embedding_qianwen_url'",
                    Integer.class);
            if (count != null && count > 0) {
                return;
            }
            jdbcTemplate.execute("ALTER TABLE `kb_model_config` " +
                    "ADD COLUMN `embedding_qianwen_url` VARCHAR(255) NOT NULL DEFAULT 'https://dashscope.aliyuncs.com/compatible-mode/v1' " +
                    "COMMENT '千问 embedding 基础 URL'");
            log.info("已自动为 kb_model_config 表添加 embedding_qianwen_url 列");
        } catch (Exception e) {
            log.warn("自动添加 kb_model_config.embedding_qianwen_url 列失败: {}", e.getMessage());
        }
    }

    private void dropKbModelConfigMilvusEnabled() {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.columns " +
                    "WHERE table_schema = DATABASE() AND table_name = 'kb_model_config' AND column_name = 'milvus_enabled'",
                    Integer.class);
            if (count == null || count == 0) {
                return;
            }
            jdbcTemplate.execute("ALTER TABLE `kb_model_config` DROP COLUMN `milvus_enabled`");
            log.info("已删除 kb_model_config 表上过期的 milvus_enabled 列");
        } catch (Exception e) {
            log.warn("删除 kb_model_config.milvus_enabled 列失败: {}", e.getMessage());
        }
    }

    private void addKbModelConfigRerankOllamaUrl() {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.columns " +
                    "WHERE table_schema = DATABASE() AND table_name = 'kb_model_config' AND column_name = 'rerank_ollama_url'",
                    Integer.class);
            if (count != null && count > 0) {
                return;
            }
            jdbcTemplate.execute("ALTER TABLE `kb_model_config` " +
                    "ADD COLUMN `rerank_ollama_url` VARCHAR(255) NOT NULL DEFAULT 'http://127.0.0.1:11434' " +
                    "COMMENT '本地 Ollama rerank 基础 URL'");
            log.info("已自动为 kb_model_config 表添加 rerank_ollama_url 列");
        } catch (Exception e) {
            log.warn("自动添加 kb_model_config.rerank_ollama_url 列失败: {}", e.getMessage());
        }
    }

    private void addKbModelConfigRerankMaxChunksPerDoc() {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.columns " +
                    "WHERE table_schema = DATABASE() AND table_name = 'kb_model_config' AND column_name = 'rerank_max_chunks_per_doc'",
                    Integer.class);
            if (count != null && count > 0) {
                return;
            }
            jdbcTemplate.execute("ALTER TABLE `kb_model_config` " +
                    "ADD COLUMN `rerank_max_chunks_per_doc` INT NOT NULL DEFAULT 512 " +
                    "COMMENT 'SiliconFlow rerank 单文档最大 chunk 数'");
            log.info("已自动为 kb_model_config 表添加 rerank_max_chunks_per_doc 列");
        } catch (Exception e) {
            log.warn("自动添加 kb_model_config.rerank_max_chunks_per_doc 列失败: {}", e.getMessage());
        }
    }

    private void addKbModelConfigAutoWrite() {
        try {
            Integer enabledCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.columns " +
                    "WHERE table_schema = DATABASE() AND table_name = 'kb_model_config' AND column_name = 'auto_write_enabled'",
                    Integer.class);
            if (enabledCount == null || enabledCount == 0) {
                jdbcTemplate.execute("ALTER TABLE `kb_model_config` " +
                        "ADD COLUMN `auto_write_enabled` TINYINT NOT NULL DEFAULT 0 " +
                        "COMMENT '0 关闭 1 开启自动回写'");
                log.info("已自动为 kb_model_config 表添加 auto_write_enabled 列");
            }

            Integer thresholdCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.columns " +
                    "WHERE table_schema = DATABASE() AND table_name = 'kb_model_config' AND column_name = 'auto_write_threshold'",
                    Integer.class);
            if (thresholdCount == null || thresholdCount == 0) {
                jdbcTemplate.execute("ALTER TABLE `kb_model_config` " +
                        "ADD COLUMN `auto_write_threshold` DECIMAL(4,3) NOT NULL DEFAULT 0.300 " +
                        "COMMENT '低置信度触发自动回写阈值'");
                log.info("已自动为 kb_model_config 表添加 auto_write_threshold 列");
            }
        } catch (Exception e) {
            log.warn("自动添加 kb_model_config 自动回写列失败: {}", e.getMessage());
        }
    }

    private void dropKbCategoryUnusedColumns() {
        String[] columns = {"embedding_model", "vector_dim", "local_threshold", "auto_write_enabled", "auto_write_threshold"};
        for (String column : columns) {
            try {
                Integer count = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM information_schema.columns " +
                        "WHERE table_schema = DATABASE() AND table_name = 'kb_category' AND column_name = '" + column + "'",
                        Integer.class);
                if (count == null || count == 0) {
                    continue;
                }
                jdbcTemplate.execute("ALTER TABLE `kb_category` DROP COLUMN `" + column + "`");
                log.info("已删除 kb_category 表上的 {} 列", column);
            } catch (Exception e) {
                log.warn("删除 kb_category.{} 列失败: {}", column, e.getMessage());
            }
        }
    }

    private void dropKbModelConfigWrongUniqueKey() {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.table_constraints " +
                    "WHERE table_schema = DATABASE() AND table_name = 'kb_model_config' AND constraint_name = 'uk_is_default'",
                    Integer.class);
            if (count == null || count == 0) {
                return;
            }
            jdbcTemplate.execute("ALTER TABLE `kb_model_config` DROP INDEX `uk_is_default`");
            log.info("已删除 kb_model_config 表上过期的 uk_is_default 唯一索引");
        } catch (Exception e) {
            log.warn("删除 kb_model_config.uk_is_default 唯一索引失败: {}", e.getMessage());
        }
    }
}
