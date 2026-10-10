package com.example.library.infrastructure.persistence;

import com.example.library.domain.model.fine.FineRecord;
import com.example.library.domain.model.fine.FineStatus;
import com.example.library.domain.repository.FineRecordRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * FineRecordRepository 的持久化适配器：把领域端口委托给 Spring Data JPA。
 */
@Component
public class FineRecordRepositoryAdapter implements FineRecordRepository {

    private final JpaFineRecordRepository jpa;

    public FineRecordRepositoryAdapter(JpaFineRecordRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public FineRecord save(FineRecord fineRecord) {
        return jpa.save(fineRecord);
    }

    @Override
    public Optional<FineRecord> findById(Long id) {
        return jpa.findById(id);
    }

    @Override
    public boolean existsUnpaidByReaderId(Long readerId) {
        return jpa.existsByReaderIdAndStatus(readerId, FineStatus.UNPAID);
    }

    @Override
    public List<FineRecord> findByReaderId(Long readerId) {
        return jpa.findByReaderId(readerId);
    }
}
