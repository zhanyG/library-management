package com.example.library.infrastructure.persistence;

import com.example.library.domain.model.circulation.BorrowPolicy;
import com.example.library.domain.model.reader.ReaderType;
import com.example.library.domain.repository.BorrowPolicyRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * BorrowPolicyRepository 的持久化适配器：把领域端口委托给 Spring Data JPA。
 */
@Component
public class BorrowPolicyRepositoryAdapter implements BorrowPolicyRepository {

    private final JpaBorrowPolicyRepository jpa;

    public BorrowPolicyRepositoryAdapter(JpaBorrowPolicyRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public BorrowPolicy save(BorrowPolicy policy) {
        return jpa.save(policy);
    }

    @Override
    public Optional<BorrowPolicy> findByReaderType(ReaderType readerType) {
        return jpa.findByReaderType(readerType);
    }

    @Override
    public List<BorrowPolicy> findAll() {
        return jpa.findAll();
    }
}
