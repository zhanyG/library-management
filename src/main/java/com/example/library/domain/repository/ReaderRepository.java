package com.example.library.domain.repository;
import com.example.library.domain.model.reader.Reader;
import java.util.Optional;

/**
 * 读者持久化端口。
 *
 * <p>领域层端口：只声明业务语义方法，不依赖任何框架。</p>
 */
public interface ReaderRepository {

    Reader save(Reader reader);
    Optional<Reader> findById(Long id);
    Optional<Reader> findByIdCardNo(String idCardNo);
    boolean existsByIdCardNo(String idCardNo);
}
