package com.enterprise.qa.rag;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 关键词检索：基于 MySQL FULLTEXT（ngram parser，天然支持中文）。
 * 使用原生 SQL 便于结合 EXPLAIN 分析执行计划、调整索引策略。
 */
@Component
public class KeywordRetriever {

    private final JdbcTemplate jdbcTemplate;

    public KeywordRetriever(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<RetrievedChunk> search(String question, int limit) {
        String sql = """
                SELECT c.id, c.doc_id, d.title, c.content,
                       MATCH(c.content) AGAINST(? IN NATURAL LANGUAGE MODE) AS score
                FROM document_chunk c
                JOIN knowledge_document d ON c.doc_id = d.id
                WHERE MATCH(c.content) AGAINST(? IN NATURAL LANGUAGE MODE)
                ORDER BY score DESC, c.id
                LIMIT ?
                """;
        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new RetrievedChunk(
                        rs.getLong("id"),
                        rs.getLong("doc_id"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getDouble("score")),
                question, question, limit);
    }
}
