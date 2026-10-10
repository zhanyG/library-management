package com.example.library.domain.model.circulation;

import com.example.library.domain.model.reader.ReaderType;
import com.example.library.exception.InvalidPolicyValueException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * 借阅规则（策略对象）：按读者类型定义最大借阅数量与借阅期限（BR-003、BR-007）。
 */
@Entity
@Table(name = "borrow_policies")
public class BorrowPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "reader_type", nullable = false, unique = true, length = 32)
    private ReaderType readerType;

    @Column(name = "max_loan_count", nullable = false)
    private int maxLoanCount;

    @Column(name = "loan_period_days", nullable = false)
    private int loanPeriodDays;

    protected BorrowPolicy() {
    }

    public BorrowPolicy(ReaderType readerType, int maxLoanCount, int loanPeriodDays) {
        this.readerType = readerType;
        this.maxLoanCount = maxLoanCount;
        this.loanPeriodDays = loanPeriodDays;
        validate();
    }

    /** 当前在借数量是否已超过上限。 */
    public boolean exceedQuota(int currentLoanCount) {
        return currentLoanCount >= maxLoanCount;
    }

    /** 按借阅期限计算应还日期（BR-007）。 */
    public LocalDate calcDueDate(LocalDate borrowDate) {
        return borrowDate.plusDays(loanPeriodDays);
    }

    /** 规则维护校验（FR-016）。 */
    public void update(int maxLoanCount, int loanPeriodDays) {
        this.maxLoanCount = maxLoanCount;
        this.loanPeriodDays = loanPeriodDays;
        validate();
    }

    private void validate() {
        if (maxLoanCount <= 0 || loanPeriodDays <= 0) {
            throw new InvalidPolicyValueException("借阅规则数值必须大于 0");
        }
    }

    public Long getId() {
        return id;
    }

    public ReaderType getReaderType() {
        return readerType;
    }

    public int getMaxLoanCount() {
        return maxLoanCount;
    }

    public int getLoanPeriodDays() {
        return loanPeriodDays;
    }
}
