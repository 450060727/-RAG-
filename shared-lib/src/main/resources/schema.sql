-- 注意：database 本身需先手工创建（spring.sql.init 是在已连上 xiaoma 库后执行的）：
--   CREATE DATABASE xiaoma DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `user` (
  `id`         INT       PRIMARY KEY AUTO_INCREMENT,
  `email`      VARCHAR(255) NOT NULL UNIQUE,
  `password`   VARCHAR(100) NOT NULL,
  `name`       VARCHAR(50)  NOT NULL,
  `status`     TINYINT      NOT NULL DEFAULT 0 COMMENT '0 正常，1 禁用',
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 后台管理相关表
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id`              INT       PRIMARY KEY AUTO_INCREMENT,
  `username`        VARCHAR(50)  NOT NULL UNIQUE,
  `password`        VARCHAR(100) NOT NULL,
  `real_name`       VARCHAR(50)  NULL,
  `phone`           VARCHAR(20)  NULL,
  `email`           VARCHAR(255) NULL,
  `avatar`          VARCHAR(255) NULL,
  `dept_id`         INT       NULL,
  `status`          TINYINT      NOT NULL DEFAULT 0 COMMENT '0 正常，1 禁用',
  `super_admin`     TINYINT      NOT NULL DEFAULT 0 COMMENT '0 普通，1 超级管理员',
  `last_login_time` DATETIME     NULL,
  `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_dept_id` (`dept_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `sys_dept` (
  `id`         INT       PRIMARY KEY AUTO_INCREMENT,
  `parent_id`  INT       NULL,
  `name`       VARCHAR(50)  NOT NULL,
  `code`       VARCHAR(50)  NOT NULL UNIQUE,
  `sort`       INT          NOT NULL DEFAULT 0,
  `status`     TINYINT      NOT NULL DEFAULT 0 COMMENT '0 正常，1 禁用',
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_parent_id` (`parent_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `sys_role` (
  `id`         INT       PRIMARY KEY AUTO_INCREMENT,
  `name`       VARCHAR(50)  NOT NULL,
  `code`       VARCHAR(50)  NOT NULL UNIQUE,
  `data_scope` VARCHAR(20)  NOT NULL DEFAULT 'ALL' COMMENT 'ALL/DEPT/DEPT_AND_CHILD/CUSTOM',
  `status`     TINYINT      NOT NULL DEFAULT 0 COMMENT '0 正常，1 禁用',
  `remark`     VARCHAR(255) NULL,
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `sys_menu` (
  `id`         INT       PRIMARY KEY AUTO_INCREMENT,
  `parent_id`  INT       NULL,
  `name`       VARCHAR(50)  NOT NULL,
  `type`       TINYINT      NOT NULL COMMENT '1 目录，2 菜单，3 按钮/API',
  `permission` VARCHAR(100) NULL COMMENT '权限字符串，如 system:user:create',
  `path`       VARCHAR(100) NULL,
  `component`  VARCHAR(100) NULL,
  `icon`       VARCHAR(50)  NULL,
  `sort`       INT          NOT NULL DEFAULT 0,
  `status`     TINYINT      NOT NULL DEFAULT 0 COMMENT '0 正常，1 禁用',
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_parent_id` (`parent_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `user_id` INT NOT NULL,
  `role_id` INT NOT NULL,
  PRIMARY KEY (`user_id`, `role_id`),
  INDEX `idx_role_id` (`role_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `sys_role_menu` (
  `role_id` INT NOT NULL,
  `menu_id` INT NOT NULL,
  PRIMARY KEY (`role_id`, `menu_id`),
  INDEX `idx_menu_id` (`menu_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `sys_role_dept` (
  `role_id` INT NOT NULL,
  `dept_id` INT NOT NULL,
  PRIMARY KEY (`role_id`, `dept_id`),
  INDEX `idx_dept_id` (`dept_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 超级管理员通过 AdminAuthService 启动时自动初始化（用户名 admin，密码 admin123）

-- 模型配置（多份：id=1 为默认配置，其他可被知识库一级分类引用）
CREATE TABLE IF NOT EXISTS `kb_model_config` (
  `id`                           INT PRIMARY KEY AUTO_INCREMENT,
  `name`                         VARCHAR(100) NOT NULL DEFAULT '' COMMENT '配置名称，如 default、分类名',
  `is_default`                   TINYINT      NOT NULL DEFAULT 0 COMMENT '1 表示默认配置，全局唯一',

  -- 1. Milvus 基础配置
  `milvus_host`                  VARCHAR(255) NOT NULL DEFAULT 'localhost',
  `milvus_port`                  INT          NOT NULL DEFAULT 19530,
  `milvus_username`              VARCHAR(100) NOT NULL DEFAULT 'root',
  `milvus_password`              VARCHAR(100) NOT NULL DEFAULT 'Milvus',
  `milvus_database`              VARCHAR(100) NOT NULL DEFAULT 'default',
  `milvus_collection`            VARCHAR(100) NOT NULL DEFAULT 'knowledge_segment',
  `milvus_vector_dim`            INT          NOT NULL DEFAULT 1024 COMMENT '向量维度，改模型后需重建 collection',
  `milvus_metric_type`           VARCHAR(20)  NOT NULL DEFAULT 'COSINE',
  `milvus_index_type`            VARCHAR(20)  NOT NULL DEFAULT 'HNSW',
  `milvus_consistency_level`     VARCHAR(20)  NOT NULL DEFAULT 'Bounded',

  -- 2. 向量计算（embedding）
  `embedding_provider`           VARCHAR(20)  NOT NULL DEFAULT 'ollama' COMMENT 'ollama / siliconflow / qianwen',
  `embedding_model`              VARCHAR(100) NOT NULL DEFAULT 'BAAI/bge-m3' COMMENT '远程 provider 使用的模型名',
  `embedding_api_key`            VARCHAR(255) NOT NULL DEFAULT '' COMMENT '远程 provider API Key',
  `embedding_base_url`           VARCHAR(255) NOT NULL DEFAULT 'https://api.siliconflow.cn/v1' COMMENT 'SiliconFlow embedding 基础 URL',
  `embedding_qianwen_url`        VARCHAR(255) NOT NULL DEFAULT 'https://dashscope.aliyuncs.com/compatible-mode/v1' COMMENT '千问 embedding 基础 URL',
  `embedding_ollama_url`         VARCHAR(255) NOT NULL DEFAULT 'http://localhost:11434/api/embed',
  `embedding_ollama_model`       VARCHAR(100) NOT NULL DEFAULT 'qwen3-embedding:0.6b-q8_0',

  -- 3. 向量校准 rerank
  `rerank_enabled`               TINYINT      NOT NULL DEFAULT 1  COMMENT '是否启用 rerank',
  `rerank_provider`              VARCHAR(20)  NOT NULL DEFAULT 'ollama' COMMENT 'ollama / siliconflow / qianwen',
  `rerank_model`                 VARCHAR(100) NOT NULL DEFAULT '' COMMENT '远程 rerank 模型名；本地为 ollama rerank 模型',
  `rerank_api_key`               VARCHAR(255) NOT NULL DEFAULT '' COMMENT '远程 rerank API Key',
  `rerank_base_url`              VARCHAR(255) NOT NULL DEFAULT 'https://api.siliconflow.cn/v1' COMMENT '远程 rerank 基础 URL',
  `rerank_ollama_url`            VARCHAR(255) NOT NULL DEFAULT 'http://127.0.0.1:11434' COMMENT '本地 Ollama rerank 基础 URL',
  `rerank_concurrency`           INT          NOT NULL DEFAULT 4 COMMENT '本地 Ollama rerank 并发数',
  `rerank_top_k`                 INT          NOT NULL DEFAULT 5 COMMENT '召回阶段 topK',
  `rerank_limit`                 INT          NOT NULL DEFAULT 5 COMMENT 'rerank 后返回条数',
  `rerank_prompt_template`       TEXT         NOT NULL COMMENT '本地 Ollama rerank prompt 模板',
  `rerank_temperature`           DECIMAL(3,2) NOT NULL DEFAULT 0.00 COMMENT '本地 rerank temperature',
  `rerank_max_chunks_per_doc`    INT          NOT NULL DEFAULT 512 COMMENT 'SiliconFlow rerank 单文档最大 chunk 数',

  -- 4. 对话模型
  `chat_provider`                VARCHAR(20)  NOT NULL DEFAULT 'ollama' COMMENT 'ollama / siliconflow / qianwen',
  `chat_model`                   VARCHAR(100) NOT NULL DEFAULT 'qwen:1.8b' COMMENT '远程/本地对话模型名',
  `chat_api_key`                 VARCHAR(255) NOT NULL DEFAULT '' COMMENT '远程对话 API Key',
  `chat_base_url`                VARCHAR(255) NOT NULL DEFAULT 'https://dashscope.aliyuncs.com/compatible-mode/v1' COMMENT '远程对话基础 URL',
  `chat_ollama_url`              VARCHAR(255) NOT NULL DEFAULT 'http://127.0.0.1:11434' COMMENT '本地 Ollama 基础 URL',
  `chat_temperature`             DECIMAL(3,2) NULL     DEFAULT 0.70 COMMENT '对话 temperature',
  `chat_max_tokens`              INT          NULL     DEFAULT 2048 COMMENT '对话最大 token',

  -- 5. 检索/聊天行为参数
  `local_threshold`              DECIMAL(4,3) NOT NULL DEFAULT 0.750 COMMENT '本地 RAG 命中阈值',
  `top_k`                        INT          NOT NULL DEFAULT 5 COMMENT '未启用 rerank 时的最终 topK',
  `default_category_id`          INT          NOT NULL DEFAULT 1,
  `history_rounds`               INT          NOT NULL DEFAULT 3,
  `history_max_chars`            INT          NOT NULL DEFAULT 3000,
  `context_threshold`            DECIMAL(4,3) NOT NULL DEFAULT 0.550 COMMENT '上下文片段过滤阈值',
  `context_max_chars`            INT          NOT NULL DEFAULT 4000,
  `auto_write_enabled`           TINYINT      NOT NULL DEFAULT 0 COMMENT '0 关闭 1 开启自动回写',
  `auto_write_threshold`         DECIMAL(4,3) NOT NULL DEFAULT 0.300 COMMENT '低置信度触发自动回写阈值',

  -- 6. 会话 / TOKEN / 验证码配置
  `jwt_secret`                   VARCHAR(255) NOT NULL DEFAULT 'xiaoma-dev-secret-key-0123456789abcdef' COMMENT 'JWT 签名密钥',
  `jwt_expire_minutes`           INT          NOT NULL DEFAULT 43200 COMMENT 'JWT 签名有效期（分钟）',
  `code_ttl_seconds`             INT          NOT NULL DEFAULT 300 COMMENT '验证码有效期（秒）',
  `code_resend_interval_seconds` INT          NOT NULL DEFAULT 60 COMMENT '验证码重发间隔（秒）',
  `session_ttl_minutes`          INT          NOT NULL DEFAULT 120 COMMENT 'Redis 会话有效期（分钟）',
  `session_renew_threshold_minutes` INT       NOT NULL DEFAULT 60 COMMENT '会话续签阈值（分钟）',

  `version`                      INT          NOT NULL DEFAULT 1 COMMENT '配置版本号，更新时 +1',
  `created_at`                   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`                   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
  -- 注意：不在这里对 is_default 加 UNIQUE，否则多个 is_default=0 的分类配置会冲突；
  --       默认配置的唯一性由业务代码（loadDefaultFromDb / saveDefault）保证。
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 知识库分类
CREATE TABLE IF NOT EXISTS `kb_category` (
  `id`              INT PRIMARY KEY AUTO_INCREMENT,
  `parent_id`       INT NULL DEFAULT 0 COMMENT '上级分类，0 为顶级',
  `name`            VARCHAR(100) NOT NULL,
  `description`     VARCHAR(500) NULL,
  `model_config_id` INT NULL COMMENT '引用的 kb_model_config.id，NULL 表示使用默认配置',
  `sort_order`      INT NOT NULL DEFAULT 0 COMMENT '排序号，越小越靠前',
  `status`          TINYINT NOT NULL DEFAULT 0 COMMENT '0 正常 1 禁用',
  `created_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_parent_id` (`parent_id`),
  INDEX `idx_model_config_id` (`model_config_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- 知识库文档（文件/手动文本/QA 对）
CREATE TABLE IF NOT EXISTS `kb_document` (
  `id`          INT PRIMARY KEY AUTO_INCREMENT,
  `category_id` INT NOT NULL,
  `title`       VARCHAR(255) NOT NULL,
  `source_type` VARCHAR(20) NOT NULL COMMENT 'UPLOAD TEXT QA_PAIR',
  `file_type`   VARCHAR(20) NULL COMMENT 'txt pdf docx md image audio video qa',
  `file_path`   VARCHAR(500) NULL,
  `file_size`   BIGINT NULL,
  `file_mime`   VARCHAR(100) NULL,
  `chunk_count` INT NOT NULL DEFAULT 0,
  `status`      TINYINT NOT NULL DEFAULT 0 COMMENT '0 正常 1 删除 2 解析中 9 解析失败',
  `created_by`  INT NULL COMMENT '管理员用户 ID',
  `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 文档切片元数据（向量 ID 对应 Milvus 主键）
CREATE TABLE IF NOT EXISTS `kb_segment` (
  `id`          INT PRIMARY KEY AUTO_INCREMENT,
  `doc_id`      INT NOT NULL,
  `category_id` INT NOT NULL,
  `content`     TEXT NOT NULL,
  `vector_id`   VARCHAR(64) NULL COMMENT 'Milvus 主键',
  `sort_order`  INT NOT NULL DEFAULT 0,
  `status`      TINYINT NOT NULL DEFAULT 0,
  `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_doc_id` (`doc_id`),
  INDEX `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 用户聊天会话
CREATE TABLE IF NOT EXISTS `kb_chat_session` (
  `id`          INT PRIMARY KEY AUTO_INCREMENT,
  `user_id`     INT NOT NULL,
  `category_id` INT NOT NULL,
  `title`       VARCHAR(200) NOT NULL,
  `status`      TINYINT NOT NULL DEFAULT 0,
  `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 聊天消息与反馈/回写状态
CREATE TABLE IF NOT EXISTS `kb_chat_message` (
  `id`                INT PRIMARY KEY AUTO_INCREMENT,
  `session_id`        INT NOT NULL,
  `role`              VARCHAR(20) NOT NULL COMMENT 'user assistant',
  `content`           TEXT NOT NULL,
  `sources_json`      TEXT NULL COMMENT '引用来源 JSON',
  `use_local`         TINYINT NOT NULL DEFAULT 0 COMMENT '是否命中本地知识库',
  `confidence`        DECIMAL(4,3) NULL COMMENT '本地检索最高相似度',
  `llm_response_json` TEXT NULL,
  `feedback`          VARCHAR(20) NULL COMMENT 'up down',
  `write_back_status` VARCHAR(20) NULL DEFAULT 'NONE' COMMENT 'NONE PENDING APPROVED REJECTED',
  `created_at`        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

