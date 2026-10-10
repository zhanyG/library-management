package com.example.library.infrastructure.persistence;

import com.example.library.domain.model.fine.FineRecord;
import com.example.library.domain.model.fine.FineStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * FineRecord 的 Spring Data JPA 接口（派生查询）。
 */
public interface JpaFineRecordRepository extends JpaRepository<FineRecord, Long> {

    boolean existsByReaderIdAndStatus(Long readerId, FineStatus status);
    List<FineRecord> findByReaderId(Long readerId);
}
