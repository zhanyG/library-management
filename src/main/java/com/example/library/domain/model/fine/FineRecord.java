package com.example.library.domain.model.fine;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 罚款记录：一次超期借阅对应一笔罚款（BR-005、BR-009）。
 */
@Entity
@Table(name = "fine_records")
public class FineRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 一次借阅最多一笔罚款。 */
    @Column(name = "loan_id", nullable = false, unique = true)
    private Long loanId;

    @Column(name = "reader_id", nullable = false)
    private Long readerId;

    @Embedded
    private Money amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private FineStatus status = FineStatus.UNPAID;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    protected FineRecord() {
    }

    public FineRecord(Long loanId, Long readerId, Money amount) {
        this.loanId = loanId;
        this.readerId = readerId;
        this.amount = amount;
        this.status = FineStatus.UNPAID;
    }

    public boolean isUnpaid() {
        return status == FineStatus.UNPAID;
    }

    public void pay() {
        if (status == FineStatus.UNPAID) {
            this.status = FineStatus.PAID;
            this.paidAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Long getLoanId() {
        return loanId;
    }

    public Long getReaderId() {
        return readerId;
    }

    public Money getAmount() {
        return amount;
    }

    public FineStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }
}
