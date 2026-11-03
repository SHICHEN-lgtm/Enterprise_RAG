package com.enterprise.qa.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 文档分块，是 RAG 检索的最小单元。content 建有 MySQL FULLTEXT(ngram) 全文索引，
 * 用于中文关键词混合检索。
 */
@Data
@Entity
@Table(name = "document_chunk",
        indexes = {
                @Index(name = "idx_chunk_doc", columnList = "doc_id")
        })
public class DocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "doc_id", nullable = false)
    private Long docId;

    /** 块序号 */
    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;

    /** 块内容 */
    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;
}
