package com.enterprise.qa.controller;

import com.enterprise.qa.common.Result;
import com.enterprise.qa.entity.QaRecord;
import com.enterprise.qa.service.RecordService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 问答记录查询接口（效果评测与历史追溯）。
 */
@RestController
@RequestMapping("/api/records")
public class RecordController {

    private final RecordService recordService;

    public RecordController(RecordService recordService) {
        this.recordService = recordService;
    }

    @GetMapping
    public Result<List<QaRecord>> recent(@RequestParam(defaultValue = "20") int limit) {
        return Result.ok(recordService.recent(limit));
    }
}
