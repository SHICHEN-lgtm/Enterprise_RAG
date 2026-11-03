-- ============================================================
-- enterprise-qa 数据库初始化脚本
-- 适用：MySQL 8.x（FULLTEXT ngram 中文检索需 MySQL 5.7.6+）
-- 执行：mysql -u root -p < sql/schema.sql
-- 说明：表结构显式管理而非依赖 hibernate ddl-auto，便于用 EXPLAIN 优化索引
-- ============================================================

CREATE DATABASE IF NOT EXISTS enterprise_qa
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE enterprise_qa;

-- 知识文档表
CREATE TABLE IF NOT EXISTS knowledge_document (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    category    VARCHAR(64)  NULL,
    content     LONGTEXT     NOT NULL,
    source      VARCHAR(128) NULL,
    created_at  DATETIME     NOT NULL,
    INDEX idx_doc_category (category),
    INDEX idx_doc_created_at (created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 文档分块表（RAG 检索最小单元）
-- content 建 FULLTEXT 索引（ngram parser 天然支持中文分词），供 MATCH...AGAINST 使用
CREATE TABLE IF NOT EXISTS document_chunk (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    doc_id       BIGINT      NOT NULL,
    chunk_index  INT         NOT NULL,
    content      LONGTEXT    NOT NULL,
    INDEX idx_chunk_doc (doc_id),
    FULLTEXT INDEX ft_chunk_content (content) WITH PARSER ngram
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 问答记录表（效果评测：延迟、token、命中来源）
CREATE TABLE IF NOT EXISTS qa_record (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    question          TEXT     NOT NULL,
    answer            LONGTEXT NOT NULL,
    model             VARCHAR(64) NULL,
    prompt_tokens     INT      NULL,
    completion_tokens INT      NULL,
    latency_ms        BIGINT   NULL,
    sources           TEXT     NULL,
    created_at        DATETIME NOT NULL,
    INDEX idx_record_created_at (created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
