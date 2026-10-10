package com.example.library.domain.repository;
import com.example.library.domain.model.circulation.LoanStatus;
import com.example.library.domain.model.circulation.Loan;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 借阅记录持久化端口。
 *
 * <p>领域层端口：只声明业务语义方法，不依赖任何框架。</p>
 */
public interface LoanRepository {

    /** 未归还状态集合（BR-003 的在借统计口径）。 */
    List<LoanStatus> ACTIVE_STATUSES = List.of(LoanStatus.BORROWED, LoanStatus.OVERDUE);

    Loan save(Loan loan);
    Optional<Loan> findById(Long id);
    List<Loan> findByReaderId(Long readerId);
    long countActiveByReaderId(Long readerId);
    boolean existsOverdueByReaderId(Long readerId, LocalDate today);
    Optional<Loan> findActiveByItemId(Long itemId);
}
