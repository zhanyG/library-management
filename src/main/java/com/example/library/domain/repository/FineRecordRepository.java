package com.example.library.domain.repository;
import com.example.library.domain.model.fine.FineRecord;
import java.util.List;
import java.util.Optional;

/**
 * 罚款记录持久化端口。
 *
 * <p>领域层端口：只声明业务语义方法，不依赖任何框架。</p>
 */
public interface FineRecordRepository {

    FineRecord save(FineRecord fineRecord);
    Optional<FineRecord> findById(Long id);
    boolean existsUnpaidByReaderId(Long readerId);
    List<FineRecord> findByReaderId(Long readerId);
}
