package com.example.library.infrastructure.persistence;

import com.example.library.domain.model.reader.Reader;
import com.example.library.domain.repository.ReaderRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * ReaderRepository 的持久化适配器：把领域端口委托给 Spring Data JPA。
 */
@Component
public class ReaderRepositoryAdapter implements ReaderRepository {

    private final JpaReaderRepository jpa;

    public ReaderRepositoryAdapter(JpaReaderRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Reader save(Reader reader) {
        return jpa.save(reader);
    }

    @Override
    public Optional<Reader> findById(Long id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<Reader> findByIdCardNo(String idCardNo) {
        return jpa.findByIdCardNo(idCardNo);
    }

    @Override
    public boolean existsByIdCardNo(String idCardNo) {
        return jpa.existsByIdCardNo(idCardNo);
    }
}
