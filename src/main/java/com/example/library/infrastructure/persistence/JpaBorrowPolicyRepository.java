package com.example.library.infrastructure.persistence;

import com.example.library.domain.model.circulation.BorrowPolicy;
import com.example.library.domain.model.reader.ReaderType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * BorrowPolicy 的 Spring Data JPA 接口（派生查询）。
 */
public interface JpaBorrowPolicyRepository extends JpaRepository<BorrowPolicy, Long> {

    Optional<BorrowPolicy> findByReaderType(ReaderType readerType);
}
