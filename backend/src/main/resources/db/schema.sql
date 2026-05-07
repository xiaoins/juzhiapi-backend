-- =====================================================
-- Juzhi API Platform - 数据库初始化脚本
-- AI 聚合站 V1
-- =====================================================

CREATE DATABASE IF NOT EXISTS ai_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ai_platform;

-- =====================================================
-- 1. 用户表
-- =====================================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username`    VARCHAR(50)  NOT NULL COMMENT '用户名',
    `email`       VARCHAR(100)          DEFAULT NULL COMMENT '邮箱',
    `phone`       VARCHAR(30)           DEFAULT NULL COMMENT '手机号',
    `password`    VARCHAR(255) NOT NULL COMMENT '密码(bcrypt加密)',
    `avatar`      VARCHAR(255)          DEFAULT NULL COMMENT '头像URL',
    `role`        VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '角色: USER/ADMIN',
    `status`      TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '状态: 1正常 0禁用',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- =====================================================
-- 2. 钱包表
-- =====================================================
DROP TABLE IF EXISTS `wallet`;
CREATE TABLE `wallet` (
    `id`              BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`         BIGINT      NOT NULL COMMENT '用户ID',
    `balance`         BIGINT      NOT NULL DEFAULT 0 COMMENT '余额(credits)',
    `total_recharge`  BIGINT      NOT NULL DEFAULT 0 COMMENT '累计充值',
    `total_used`      BIGINT      NOT NULL DEFAULT 0 COMMENT '累计消耗',
    `created_at`      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钱包表';

-- =====================================================
-- 3. 钱包流水表
-- =====================================================
DROP TABLE IF EXISTS `wallet_log`;
CREATE TABLE `wallet_log` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`         BIGINT       NOT NULL COMMENT '用户ID',
    `type`            VARCHAR(30)  NOT NULL COMMENT '类型: RECHARGE/CONSUME/ADMIN_ADD/ADMIN_DEDUCT/REFUND',
    `amount`          BIGINT       NOT NULL COMMENT '变动金额(正增负减)',
    `before_balance`  BIGINT       NOT NULL COMMENT '变动前余额',
    `after_balance`   BIGINT       NOT NULL COMMENT '变动后余额',
    `remark`          VARCHAR(255)          DEFAULT NULL COMMENT '备注',
    `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钱包流水表';

-- =====================================================
-- 4. API Key 表
-- =====================================================
DROP TABLE IF EXISTS `api_key`;
CREATE TABLE `api_key` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`       BIGINT       NOT NULL COMMENT '用户ID',
    `name`          VARCHAR(100)          DEFAULT NULL COMMENT 'Key名称',
    `api_key`       VARCHAR(255) NOT NULL COMMENT 'API Key(SHA-256哈希)',
    `key_prefix`    VARCHAR(50)           DEFAULT NULL COMMENT 'Key前缀(用于展示)',
    `status`        TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '状态: 1正常 0禁用',
    `total_calls`   BIGINT       NOT NULL DEFAULT 0 COMMENT '累计调用次数',
    `total_cost`    BIGINT       NOT NULL DEFAULT 0 COMMENT '累计消耗(credits)',
    `last_used_at`  DATETIME              DEFAULT NULL COMMENT '最后使用时间',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_api_key` (`api_key`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API Key表';

-- =====================================================
-- 5. AI 模型表
-- =====================================================
DROP TABLE IF EXISTS `ai_model`;
CREATE TABLE `ai_model` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `display_name`  VARCHAR(100) NOT NULL COMMENT '显示名称',
    `model_name`    VARCHAR(100) NOT NULL COMMENT '真实模型名',
    `provider`      VARCHAR(50)  NOT NULL COMMENT '供应商',
    `input_price`   BIGINT       NOT NULL DEFAULT 0 COMMENT '输入价格(credits/1K tokens)',
    `output_price`  BIGINT       NOT NULL DEFAULT 0 COMMENT '输出价格(credits/1K tokens)',
    `sort`          INT          NOT NULL DEFAULT 0 COMMENT '排序权重',
    `enabled`       TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否启用: 1是 0否',
    `recommended`   TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否推荐: 1是 0否',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI模型表';

-- =====================================================
-- 6. 对话会话表
-- =====================================================
DROP TABLE IF EXISTS `chat_session`;
CREATE TABLE `chat_session` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     BIGINT       NOT NULL COMMENT '用户ID',
    `title`       VARCHAR(255)          DEFAULT NULL COMMENT '会话标题',
    `model_name`  VARCHAR(100)          DEFAULT NULL COMMENT '使用的模型',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对话会话表';

-- =====================================================
-- 7. 对话消息表
-- =====================================================
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `session_id`  BIGINT       NOT NULL COMMENT '会话ID',
    `user_id`     BIGINT       NOT NULL COMMENT '用户ID',
    `role`        VARCHAR(20)  NOT NULL COMMENT '角色: user/assistant/system',
    `content`     TEXT         NOT NULL COMMENT '消息内容',
    `model_name`  VARCHAR(100)          DEFAULT NULL COMMENT '回复模型(assistant消息时记录)',
    `token_count` INT          NOT NULL DEFAULT 0 COMMENT 'token数',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_session_id` (`session_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对话消息表';

-- =====================================================
-- 8. API调用日志表
-- =====================================================
DROP TABLE IF EXISTS `api_usage_log`;
CREATE TABLE `api_usage_log` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`           BIGINT       NOT NULL COMMENT '用户ID',
    `api_key_id`        BIGINT                DEFAULT NULL COMMENT 'API Key ID(可为NULL,网页聊天为NULL)',
    `session_id`        BIGINT                DEFAULT NULL COMMENT '会话ID(网页聊天时有值)',
    `model_name`        VARCHAR(100)          DEFAULT NULL COMMENT '模型名',
    `provider`          VARCHAR(50)           DEFAULT NULL COMMENT '供应商',
    `prompt_tokens`     INT          NOT NULL DEFAULT 0 COMMENT '输入token数',
    `completion_tokens` INT          NOT NULL DEFAULT 0 COMMENT '输出token数',
    `total_tokens`      INT          NOT NULL DEFAULT 0 COMMENT '总token数',
    `cost`              BIGINT       NOT NULL DEFAULT 0 COMMENT '消耗金额(credits)',
    `status`            VARCHAR(30)  NOT NULL DEFAULT 'SUCCESS' COMMENT '状态: SUCCESS/FAILED',
    `error_message`     TEXT                  DEFAULT NULL COMMENT '错误信息',
    `request_ip`        VARCHAR(100)          DEFAULT NULL COMMENT '请求IP',
    `latency_ms`        INT                   DEFAULT NULL COMMENT '响应耗时(ms)',
    `created_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API调用日志表';

-- =====================================================
-- 9. 充值订单表
-- =====================================================
DROP TABLE IF EXISTS `recharge_order`;
CREATE TABLE `recharge_order` (
    `id`          BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     BIGINT          NOT NULL COMMENT '用户ID',
    `order_no`    VARCHAR(100)    NOT NULL COMMENT '订单号',
    `amount`      DECIMAL(10, 2)  NOT NULL COMMENT '充值金额(元)',
    `credits`     BIGINT          NOT NULL COMMENT '充值credits',
    `status`      VARCHAR(30)     NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/PAID/CANCELLED',
    `pay_type`    VARCHAR(30)             DEFAULT NULL COMMENT '支付方式',
    `paid_at`     DATETIME                 DEFAULT NULL COMMENT '支付时间',
    `remark`      VARCHAR(500)            DEFAULT NULL COMMENT '备注',
    `created_at`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充值订单表';

-- =====================================================
-- 初始数据
-- =====================================================

-- 插入管理员账号 (默认密码: password, 首次登录后请修改)
INSERT INTO `user` (`username`, `password`, `role`, `status`) VALUES
('admin', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'ADMIN', 1);

-- 初始化管理员钱包 (赠送100万credits用于测试)
INSERT IGNORE INTO `wallet` (`user_id`, `balance`, `total_recharge`, `total_used`) VALUES
(1, 1000000, 1000000, 0);

-- 插入初始模型数据
INSERT INTO `ai_model` (`display_name`, `model_name`, `provider`, `input_price`, `output_price`, `sort`, `enabled`, `recommended`) VALUES
('DeepSeek V3', 'deepseek-chat', 'DeepSeek', 1, 2, 1, 1, 1),
('DeepSeek R1', 'deepseek-reasoner', 'DeepSeek', 5, 15, 2, 1, 1),
('通义千问 Turbo', 'qwen-turbo', 'Qwen', 1, 2, 3, 1, 0),
('通义千问 Plus', 'qwen-plus', 'Qwen', 2, 8, 4, 1, 1),
('通义千问 Max', 'qwen-max', 'Qwen', 4, 20, 5, 1, 0),
('豆包 Pro', 'doubao-pro-32k', 'ByteDance', 2, 10, 6, 1, 0),
('GPT-4o Mini', 'gpt-4o-mini', 'OpenAI', 3, 12, 7, 1, 1),
('GPT-4o', 'gpt-4o', 'OpenAI', 15, 60, 8, 1, 1),
('Gemini 1.5 Flash', 'gemini-1.5-flash', 'Google', 1, 3, 9, 1, 0),
('Gemini 1.5 Pro', 'gemini-1.5-pro', 'Google', 8, 25, 10, 1, 0),
('Claude 3 Haiku', 'claude-3-haiku-20240307', 'Anthropic', 5, 20, 11, 1, 0),
('Claude 3.5 Sonnet', 'claude-3-5-sonnet-20240620', 'Anthropic', 15, 75, 12, 1, 1);
