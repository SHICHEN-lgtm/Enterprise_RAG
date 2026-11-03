package com.enterprise.qa.rag;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * RAG 检索配置，对应 application.yml 的 rag 节点。
 */
@Data
@Component
@ConfigurationProperties(prefix = "rag")
public class RagProperties {

    /** 文本分块字符数 */
    private int chunkSize = 600;
    /** 分块重叠，保留上下文 */
    private int chunkOverlap = 80;
    /** 检索返回候选片段数 */
    private int topK = 4;
    /** 关键词/向量权重 */
    private Weights weights = new Weights();
    private Vector vector = new Vector();

    @Data
    public static class Weights {
        private double keyword = 0.6;
        private double vector = 0.4;
    }

    @Data
    public static class Vector {
        private boolean enabled = false;
        private String baseUrl = "";
        private String apiKey = "";
        private String model = "";
        private int dimension = 1536;
    }
}
