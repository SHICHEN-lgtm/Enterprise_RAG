package com.enterprise.qa.service;

import com.enterprise.qa.common.BusinessException;
import com.enterprise.qa.dto.DocumentRequest;
import com.enterprise.qa.entity.DocumentChunk;
import com.enterprise.qa.entity.KnowledgeDocument;
import com.enterprise.qa.rag.RagProperties;
import com.enterprise.qa.rag.TextChunker;
import com.enterprise.qa.repository.DocumentChunkRepository;
import com.enterprise.qa.repository.KnowledgeDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

/**
 * 知识文档管理：入库时全文切块，供 RAG 检索使用。
 */
@Service
public class DocumentService {

    private final KnowledgeDocumentRepository documentRepository;
    private final DocumentChunkRepository chunkRepository;
    private final TextChunker textChunker;
    private final RagProperties ragProperties;

    public DocumentService(KnowledgeDocumentRepository documentRepository,
                           DocumentChunkRepository chunkRepository,
                           TextChunker textChunker,
                           RagProperties ragProperties) {
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.textChunker = textChunker;
        this.ragProperties = ragProperties;
    }

    /** 新增文档并切块入库 */
    @Transactional
    public KnowledgeDocument add(DocumentRequest req) {
        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setTitle(req.title());
        doc.setCategory(req.category());
        doc.setContent(req.content());
        doc.setSource(req.source());
        doc.setCreatedAt(LocalDateTime.now());
        doc = documentRepository.save(doc);

        List<String> chunks = textChunker.chunk(req.content(),
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
        return doc;
    }

    /** 删除文档及其分块 */
    @Transactional
    public void delete(Long id) {
        if (!documentRepository.existsById(id)) {
            throw new BusinessException(404, "文档不存在: " + id);
        }
        chunkRepository.deleteByDocId(id);
        documentRepository.deleteById(id);
    }

    /** 文档列表（含分块数） */
    public List<KnowledgeDocument> list() {
        return documentRepository.findAll();
    }
}
