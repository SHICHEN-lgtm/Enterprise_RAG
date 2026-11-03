package com.enterprise.qa.rag;

/**
 * 检索命中片段。score 为融合后的相关度（越大越相关）。
 */
public record RetrievedChunk(Long id, Long docId, String docTitle, String content, double score) {
}
