package com.example.library.domain.repository;
import com.example.library.domain.model.circulation.BorrowPolicy;
import com.example.library.domain.model.reader.ReaderType;
import java.util.List;
import java.util.Optional;

/**
 * 借阅规则持久化端口。
 *
 * <p>领域层端口：只声明业务语义方法，不依赖任何框架。</p>
 */
public interface BorrowPolicyRepository {

    BorrowPolicy save(BorrowPolicy policy);
    Optional<BorrowPolicy> findByReaderType(ReaderType readerType);
    List<BorrowPolicy> findAll();
}
