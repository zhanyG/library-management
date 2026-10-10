package com.example.library.domain.model.circulation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TC-045 Loan 超期判定与归还状态单元测试。
 */
class LoanTest {

    private static final LocalDate BORROW_DATE = LocalDate.of(2026, 10, 1);

    private Loan newLoan(LocalDate dueDate) {
        return new Loan(1L, 1L, 1L, 1L, BORROW_DATE, dueDate);
    }

    @Test
    @DisplayName("未到应还日期不算超期")
    void notOverdueBeforeDueDate() {
        Loan loan = newLoan(BORROW_DATE.plusDays(30));

        assertThat(loan.isOverdue(BORROW_DATE.plusDays(29))).isFalse();
        assertThat(loan.overdueDays(BORROW_DATE.plusDays(29))).isZero();
        assertThat(loan.isActive()).isTrue();
    }

    @Test
    @DisplayName("超过应还日期按天计算超期")
    void overdueDays() {
        Loan loan = newLoan(LocalDate.of(2026, 10, 10));

        LocalDate today = LocalDate.of(2026, 10, 11);

        assertThat(loan.isOverdue(today)).isTrue();
        assertThat(loan.overdueDays(today)).isEqualTo(1);
    }

    @Test
    @DisplayName("登记归还后状态为 RETURNED 且不再算超期")
    void markReturned() {
        Loan loan = newLoan(LocalDate.of(2026, 10, 10));
        LocalDate returnDate = LocalDate.of(2026, 10, 20);

        loan.markReturned(returnDate);

        assertThat(loan.getReturnDate()).isEqualTo(returnDate);
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.RETURNED);
        assertThat(loan.isActive()).isFalse();
        assertThat(loan.isOverdue(returnDate)).isFalse();
    }

    @Test
    @DisplayName("标记超期后仍属于未归还")
    void markOverdue() {
        Loan loan = newLoan(LocalDate.of(2026, 10, 10));

        loan.markOverdue();

        assertThat(loan.getStatus()).isEqualTo(LoanStatus.OVERDUE);
        assertThat(loan.isActive()).isTrue();
    }
}
