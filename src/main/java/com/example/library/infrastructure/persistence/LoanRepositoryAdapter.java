package com.example.library.infrastructure.persistence;

import com.example.library.domain.model.circulation.Loan;
import com.example.library.domain.repository.LoanRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * LoanRepository 的持久化适配器：把领域端口委托给 Spring Data JPA。
 */
@Component
public class LoanRepositoryAdapter implements LoanRepository {

    private final JpaLoanRepository jpa;

    public LoanRepositoryAdapter(JpaLoanRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Loan save(Loan loan) {
        return jpa.save(loan);
    }

    @Override
    public Optional<Loan> findById(Long id) {
        return jpa.findById(id);
    }

    @Override
    public List<Loan> findByReaderId(Long readerId) {
        return jpa.findByReaderId(readerId);
    }

    @Override
    public long countActiveByReaderId(Long readerId) {
        return jpa.countByReaderIdAndStatusIn(readerId, LoanRepository.ACTIVE_STATUSES);
    }

    @Override
    public boolean existsOverdueByReaderId(Long readerId, LocalDate today) {
        return jpa.existsByReaderIdAndStatusInAndDueDateBefore(readerId, LoanRepository.ACTIVE_STATUSES, today);
    }

    @Override
    public Optional<Loan> findActiveByItemId(Long itemId) {
        return jpa.findFirstByItemIdAndStatusIn(itemId, LoanRepository.ACTIVE_STATUSES);
    }
}
