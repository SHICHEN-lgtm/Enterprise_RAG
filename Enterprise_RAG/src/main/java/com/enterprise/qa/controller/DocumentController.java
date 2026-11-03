package com.enterprise.qa.controller;

import com.enterprise.qa.common.Result;
import com.enterprise.qa.dto.DocumentRequest;
import com.enterprise.qa.entity.KnowledgeDocument;
import com.enterprise.qa.service.DocumentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 知识文档管理接口。
 */
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    public Result<KnowledgeDocument> add(@Valid @RequestBody DocumentRequest request) {
        return Result.ok(documentService.add(request));
    }

    @GetMapping
    public Result<List<KnowledgeDocument>> list() {
        return Result.ok(documentService.list());
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        documentService.delete(id);
        return Result.ok();
    }
}
