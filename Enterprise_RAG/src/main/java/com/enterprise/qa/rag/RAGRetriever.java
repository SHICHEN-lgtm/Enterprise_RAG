package com.enterprise.qa.rag;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * RAG 混合检索：关键词检索为主，向量检索为可插拔增强，二者归一化加权融合后按相关度排序。
 * 未启用向量时退化为纯关键词检索，保证开箱可跑。
 */
@Service
public class RAGRetriever {

    private final KeywordRetriever keywordRetriever;
    private final VectorRetriever vectorRetriever;
    private final RagProperties props;

    public RAGRetriever(KeywordRetriever keywordRetriever, VectorRetriever vectorRetriever, RagProperties props) {
        this.keywordRetriever = keywordRetriever;
        this.vectorRetriever = vectorRetriever;
        this.props = props;
    }

    public List<RetrievedChunk> retrieve(String question) {
        int topK = props.getTopK();
        int candidate = Math.max(topK * 2, 10);

        List<RetrievedChunk> kwHits = keywordRetriever.search(question, candidate);
        List<RetrievedChunk> vecHits = vectorRetriever.isEnabled()
                ? vectorRetriever.search(question, candidate)
                : List.of();

        return fuse(kwHits, vecHits, topK);
    }

    /** 归一化分数后按权重线性融合，去重并按相关度降序取 topK。 */
    private List<RetrievedChunk> fuse(List<RetrievedChunk> kwHits, List<RetrievedChunk> vecHits, int topK) {
        double kwMax = kwHits.stream().mapToDouble(RetrievedChunk::score).max().orElse(1.0);
        double vecMax = vecHits.stream().mapToDouble(RetrievedChunk::score).max().orElse(1.0);

        // chunkId -> [kwScore, vecScore]
        Map<Long, double[]> scores = new LinkedHashMap<>();
        for (RetrievedChunk c : kwHits) {
            scores.put(c.id(), new double[]{c.score() / (kwMax > 0 ? kwMax : 1), 0});
        }
        for (RetrievedChunk c : vecHits) {
            double[] e = scores.computeIfAbsent(c.id(), k -> new double[]{0, 0});
            e[1] = c.score() / (vecMax > 0 ? vecMax : 1);
        }

        Map<Long, RetrievedChunk> origin = new HashMap<>();
        for (RetrievedChunk c : kwHits) {
            origin.put(c.id(), c);
        }
        for (RetrievedChunk c : vecHits) {
            origin.putIfAbsent(c.id(), c);
        }

        double wk = props.getWeights().getKeyword();
        double wv = props.getWeights().getVector();

        return scores.entrySet().stream()
                .map(e -> {
                    RetrievedChunk c = origin.get(e.getKey());
                    double[] s = e.getValue();
                    double merged = wk * s[0] + wv * s[1];
                    return new RetrievedChunk(c.id(), c.docId(), c.docTitle(), c.content(), merged);
                })
                .sorted(Comparator.comparingDouble(RetrievedChunk::score).reversed())
                .limit(topK)
                .toList();
    }
}
