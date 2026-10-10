package com.example.library.infrastructure.persistence;

import com.example.library.domain.model.reader.Reader;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Reader 的 Spring Data JPA 接口（派生查询）。
 */
public interface JpaReaderRepository extends JpaRepository<Reader, Long> {

    boolean existsByIdCardNo(String idCardNo);
    Optional<Reader> findByIdCardNo(String idCardNo);
}
