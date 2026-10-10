package com.example.library.domain.model.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 馆藏副本（抽象实体）：承载唯一条码与可借状态（BR-006）。
 *
 * <p>继承映射：单表策略；鉴别列 {@code item_kind} 区分 Book / Magazine / Thesis，
 * {@code item_type} 列保存业务上的 5 种借出物类型。</p>
 */
@Entity
@Table(name = "library_items")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "item_kind", discriminatorType = DiscriminatorType.STRING)
public abstract class LibraryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 条码：ITEM + 年份 + 6 位序号，全局唯一。 */
    @Column(name = "barcode", nullable = false, unique = true, length = 32)
    private String barcode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "title_id", nullable = false)
    private BookTitle bookTitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 32)
    private ItemType itemType;

    @Column(name = "location", length = 64)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private ItemStatus status = ItemStatus.AVAILABLE;

    @Column(name = "loanable", nullable = false)
    private boolean loanable = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected LibraryItem() {
    }

    protected LibraryItem(String barcode, ItemType itemType, String location) {
        this.barcode = barcode;
        this.itemType = itemType;
        this.location = location;
        this.loanable = itemType.loanableByDefault();
    }

    /** 由题名侧建立组合关系时回调。 */
    public void attachTo(BookTitle title) {
        this.bookTitle = title;
    }

    /** 是否可借（状态为 AVAILABLE 且允许外借）。 */
    public boolean isAvailable() {
        return status == ItemStatus.AVAILABLE;
    }

    public boolean isOnLoan() {
        return status == ItemStatus.ON_LOAN;
    }

    public boolean isReserved() {
        return status == ItemStatus.RESERVED;
    }

    public boolean isLoanable() {
        return loanable && itemType.loanableByDefault();
    }

    public void markOnLoan() {
        this.status = ItemStatus.ON_LOAN;
    }

    public void markAvailable() {
        this.status = ItemStatus.AVAILABLE;
    }

    public void markReserved() {
        this.status = ItemStatus.RESERVED;
    }

    public void withdraw() {
        this.status = ItemStatus.WITHDRAWN;
    }

    public void markLost() {
        this.status = ItemStatus.LOST;
    }

    public Long getId() {
        return id;
    }

    public String getBarcode() {
        return barcode;
    }

    public BookTitle getBookTitle() {
        return bookTitle;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public String getLocation() {
        return location;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
