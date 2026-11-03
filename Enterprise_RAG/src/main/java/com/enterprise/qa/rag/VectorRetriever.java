package com.enterprise.qa.rag;

import java.util.List;

/**
 * 向量语义检索接口。默认由 NoopVectorRetriever 提供空实现（不启用）；
 * 接入 OpenAI 兼容 Embedding 服务时，用具体实现替换该 Bean 即开启向量分支，
 * 与关键词检索构成混合加权检索。
 */
public interface VectorRetriever {

    /** 是否启用向量分支 */
    boolean isEnabled();

    List<RetrievedChunk> search(String question, int limit);
}
