package com.enterprise.qa.repository;

import com.enterprise.qa.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {

    List<DocumentChunk> findByDocIdOrderByChunkIndexAsc(Long docId);

    long deleteByDocId(Long docId);
}
