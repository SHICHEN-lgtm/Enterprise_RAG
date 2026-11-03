package com.enterprise.qa.repository;

import com.enterprise.qa.entity.QaRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QaRecordRepository extends JpaRepository<QaRecord, Long> {
}
