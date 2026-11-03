package com.enterprise.qa.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 企业内部知识文档。入库后会被切分为 Chunk 供 RAG 检索。
 */
@Data
@Entity
@Table(name = "knowledge_document",
        indexes = {
                @Index(name = "idx_doc_category", columnList = "category"),
                @Index(name = "idx_doc_created_at", columnList = "created_at")
        })
public class KnowledgeDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 文档标题 */
    @Column(nullable = false, length = 255)
    private String title;

    /** 分类，如：制度流程 / 产品手册 / 技术文档 / 人力资源 */
    @Column(length = 64)
    private String category;

    /** 文档全文 */
    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    /** 来源 / 作者 */
    @Column(length = 128)
    private String source;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
