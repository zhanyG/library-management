package com.example.library.infrastructure.persistence;

import com.example.library.domain.model.reader.BorrowCard;
import com.example.library.domain.model.reader.CardStatus;
import com.example.library.domain.repository.BorrowCardRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * BorrowCardRepository 的持久化适配器：把领域端口委托给 Spring Data JPA。
 */
@Component
public class BorrowCardRepositoryAdapter implements BorrowCardRepository {

    private final JpaBorrowCardRepository jpa;

    public BorrowCardRepositoryAdapter(JpaBorrowCardRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public BorrowCard save(BorrowCard card) {
        return jpa.save(card);
    }

    @Override
    public Optional<BorrowCard> findById(Long id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<BorrowCard> findByCardNo(String cardNo) {
        return jpa.findByCardNo(cardNo);
    }

    @Override
    public Optional<BorrowCard> findByReaderId(Long readerId) {
        return jpa.findByReaderId(readerId);
    }

    @Override
    public boolean existsByCardNo(String cardNo) {
        return jpa.existsByCardNo(cardNo);
    }

    @Override
    public boolean existsValidCardByReaderId(Long readerId) {
        return jpa.existsByReaderIdAndStatus(readerId, CardStatus.VALID);
    }

    @Override
    public long count() {
        return jpa.count();
    }
}
