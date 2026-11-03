package com.enterprise.qa.service;

import com.enterprise.qa.entity.QaRecord;
import com.enterprise.qa.repository.QaRecordRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 问答记录查询，用于效果评测与历史追溯。
 */
@Service
public class RecordService {

    private final QaRecordRepository recordRepository;

    public RecordService(QaRecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    /** 最近 N 条问答记录（按时间倒序） */
    public List<QaRecord> recent(int limit) {
        return recordRepository.findAll(
                        PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt")))
                .getContent();
    }
}
