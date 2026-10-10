package com.example.library.domain.model.reader;

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
 * 借阅证实体：承载读者姓名、院系或单位、借阅证号（BR-001、BR-002）。
 */
@Entity
@Table(name = "borrow_cards")
public class BorrowCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 借阅证号：CARD + 年份 + 6 位序号，全局唯一。 */
    @Column(name = "card_no", nullable = false, unique = true, length = 32)
    private String cardNo;

    /** 一个读者最多一张有效借阅证。 */
    @Column(name = "reader_id", nullable = false, unique = true)
    private Long readerId;

    @Column(name = "owner_name", nullable = false, length = 64)
    private String ownerName;

    @Column(name = "department", nullable = false, length = 64)
    private String department;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private CardStatus status = CardStatus.VALID;

    protected BorrowCard() {
    }

    public BorrowCard(String cardNo, Long readerId, String ownerName, String department, LocalDate issueDate) {
        this.cardNo = cardNo;
        this.readerId = readerId;
        this.ownerName = ownerName;
        this.department = department;
        this.issueDate = issueDate;
        this.status = CardStatus.VALID;
    }

    /** 借阅证是否可用于借书（BR-002）。 */
    public boolean isValid() {
        return status == CardStatus.VALID;
    }

    public void revoke() {
        this.status = CardStatus.REVOKED;
    }

    public void markLost() {
        this.status = CardStatus.LOST;
    }

    public Long getId() {
        return id;
    }

    public String getCardNo() {
        return cardNo;
    }

    public Long getReaderId() {
        return readerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getDepartment() {
        return department;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public CardStatus getStatus() {
        return status;
    }
}
