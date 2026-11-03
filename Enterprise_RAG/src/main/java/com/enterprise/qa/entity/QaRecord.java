package com.enterprise.qa.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 问答记录，用于效果评测与历史追溯（响应延迟、token 消耗、引用来源）。
 */
@Data
@Entity
@Table(name = "qa_record", indexes = {
        @Index(name = "idx_record_created_at", columnList = "created_at")
})
public class QaRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String answer;

    /** 使用的模型 */
    @Column(length = 64)
    private String model;

    @Column(name = "prompt_tokens")
    private Integer promptTokens;

    @Column(name = "completion_tokens")
    private Integer completionTokens;

    /** 响应耗时（毫秒），用于评测响应延迟指标 */
    @Column(name = "latency_ms")
    private Long latencyMs;

    /** 命中的知识片段来源，JSON 数组字符串 */
    @Lob
    @Column(name = "sources", columnDefinition = "TEXT")
    private String sources;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
