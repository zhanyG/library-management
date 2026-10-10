package com.example.library.domain.model.circulation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 借阅记录实体：表达借出—归还的完整生命周期（BR-007、BR-008）。
 */
@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_id", nullable = false)
    private Long cardId;

    @Column(name = "reader_id", nullable = false)
    private Long readerId;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "librarian_id", nullable = false)
    private Long librarianId;

    @Column(name = "borrow_date", nullable = false)
    private LocalDate borrowDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private LoanStatus status = LoanStatus.BORROWED;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected Loan() {
    }

    public Loan(Long cardId, Long readerId, Long itemId, Long librarianId,
                LocalDate borrowDate, LocalDate dueDate) {
        this.cardId = cardId;
        this.readerId = readerId;
        this.itemId = itemId;
        this.librarianId = librarianId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.status = LoanStatus.BORROWED;
    }

    /** 是否未归还。 */
    public boolean isActive() {
        return status == LoanStatus.BORROWED || status == LoanStatus.OVERDUE;
    }

    /** 是否已超期（BR-008 的判定基础）。 */
    public boolean isOverdue(LocalDate today) {
        return isActive() && today.isAfter(dueDate);
    }

    /** 相对应还日期的超期天数，未超期为 0。 */
    public int overdueDays(LocalDate today) {
        if (!isOverdue(today)) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(dueDate, today);
    }

    /** 登记归还（FR-012）。 */
    public void markReturned(LocalDate returnDate) {
        this.returnDate = returnDate;
        this.status = LoanStatus.RETURNED;
    }

    /** 标记超期（后台扫描任务使用）。 */
    public void markOverdue() {
        if (isActive()) {
            this.status = LoanStatus.OVERDUE;
        }
    }

    public Long getId() {
        return id;
    }

    public Long getCardId() {
        return cardId;
    }

    public Long getReaderId() {
        return readerId;
    }

    public Long getItemId() {
        return itemId;
    }

    public Long getLibrarianId() {
        return librarianId;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
