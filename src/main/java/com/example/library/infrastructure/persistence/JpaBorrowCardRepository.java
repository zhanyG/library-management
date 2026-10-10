package com.example.library.infrastructure.persistence;

import com.example.library.domain.model.reader.BorrowCard;
import com.example.library.domain.model.reader.CardStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * BorrowCard 的 Spring Data JPA 接口（派生查询）。
 */
public interface JpaBorrowCardRepository extends JpaRepository<BorrowCard, Long> {

    Optional<BorrowCard> findByCardNo(String cardNo);
    Optional<BorrowCard> findByReaderId(Long readerId);
    boolean existsByCardNo(String cardNo);
    boolean existsByReaderIdAndStatus(Long readerId, CardStatus status);
}
