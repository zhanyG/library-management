package com.example.library.domain.repository;
import com.example.library.domain.model.reader.BorrowCard;
import java.util.Optional;

/**
 * 借阅证持久化端口。
 *
 * <p>领域层端口：只声明业务语义方法，不依赖任何框架。</p>
 */
public interface BorrowCardRepository {

    BorrowCard save(BorrowCard card);
    Optional<BorrowCard> findById(Long id);
    Optional<BorrowCard> findByCardNo(String cardNo);
    Optional<BorrowCard> findByReaderId(Long readerId);
    boolean existsByCardNo(String cardNo);
    boolean existsValidCardByReaderId(Long readerId);
    long count();
}
