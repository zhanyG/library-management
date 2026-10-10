package com.example.library.infrastructure.persistence;

import java.util.Collection;
import com.example.library.domain.model.circulation.Loan;
import com.example.library.domain.model.circulation.LoanStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Loan 的 Spring Data JPA 接口（派生查询）。
 */
public interface JpaLoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByReaderId(Long readerId);
    long countByReaderIdAndStatusIn(Long readerId, Collection<LoanStatus> statuses);
    boolean existsByReaderIdAndStatusInAndDueDateBefore(Long readerId, Collection<LoanStatus> statuses, LocalDate today);
    Optional<Loan> findFirstByItemIdAndStatusIn(Long itemId, Collection<LoanStatus> statuses);
}
