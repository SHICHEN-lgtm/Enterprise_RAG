package com.enterprise.qa.rag;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 未配置 Embedding 服务时的默认向量检索实现：不参与检索。
 * 配置 rag.vector.enabled=true 并实现 VectorRetriever 后替换本 Bean。
 */
@Component
public class NoopVectorRetriever implements VectorRetriever {

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public List<RetrievedChunk> search(String question, int limit) {
        return List.of();
    }
}
