package com.enterprise.qa.config;

import com.enterprise.qa.entity.DocumentChunk;
import com.enterprise.qa.entity.KnowledgeDocument;
import com.enterprise.qa.rag.RagProperties;
import com.enterprise.qa.rag.TextChunker;
import com.enterprise.qa.repository.DocumentChunkRepository;
import com.enterprise.qa.repository.KnowledgeDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.IntStream;

/**
 * 启动初始化：若 document_chunk 为空，则把库中已有文档切块入库，保证种子数据可被 RAG 检索。
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final KnowledgeDocumentRepository documentRepository;
    private final DocumentChunkRepository chunkRepository;
    private final TextChunker textChunker;
    private final RagProperties ragProperties;

    public DataInitializer(KnowledgeDocumentRepository documentRepository,
                           DocumentChunkRepository chunkRepository,
                           TextChunker textChunker,
                           RagProperties ragProperties) {
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.textChunker = textChunker;
        this.ragProperties = ragProperties;
    }

    @Override
    public void run(String... args) {
        if (chunkRepository.count() > 0) {
            log.info("document_chunk 非空，跳过切块初始化");
            return;
        }
        List<KnowledgeDocument> docs = documentRepository.findAll();
        for (KnowledgeDocument doc : docs) {
            List<String> chunks = textChunker.chunk(doc.getContent(),
                    ragProperties.getChunkSize(), ragProperties.getChunkOverlap());
            List<DocumentChunk> entities = IntStream.range(0, chunks.size())
                    .mapToObj(i -> {
                        DocumentChunk c = new DocumentChunk();
                        c.setDocId(doc.getId());
                        c.setChunkIndex(i);
                        c.setContent(chunks.get(i));
                        return c;
                    })
                    .toList();
            chunkRepository.saveAll(entities);
            log.info("文档[id={}] 已切分为 {} 块", doc.getId(), chunks.size());
        }
        log.info("知识库切块初始化完成，共 {} 个分块", chunkRepository.count());
    }
}
