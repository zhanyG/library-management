package com.example.library.domain.model.catalog;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * 图书标题（书目元数据）：检索与预约的对象，组合多个馆藏副本。
 */
@Entity
@Table(name = "book_titles")
public class BookTitle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "author", length = 120)
    private String author;

    @Column(name = "isbn", nullable = false, unique = true, length = 32)
    private String isbn;

    @Column(name = "publisher", length = 120)
    private String publisher;

    @Column(name = "published_year")
    private Integer publishedYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 32)
    private ItemType itemType;

    @Column(name = "loanable", nullable = false)
    private boolean loanable = true;

    @OneToMany(mappedBy = "bookTitle", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LibraryItem> items = new ArrayList<>();

    protected BookTitle() {
    }

    public BookTitle(String name, String author, String isbn, String publisher,
                     Integer publishedYear, ItemType itemType) {
        this.name = name;
        this.author = author;
        this.isbn = isbn;
        this.publisher = publisher;
        this.publishedYear = publishedYear;
        this.itemType = itemType;
        this.loanable = itemType.loanableByDefault();
    }

    /** 组合关系：题名新增副本。 */
    public void addItem(LibraryItem item) {
        items.add(item);
        item.attachTo(this);
    }

    /** 可借副本数量（FR-010）。 */
    public int availableItemCount() {
        return (int) items.stream().filter(LibraryItem::isAvailable).count();
    }

    public int totalItemCount() {
        return items.size();
    }

    /** 该题名是否可外借。 */
    public boolean isLoanable() {
        return loanable && itemType.loanableByDefault();
    }

    public void markNotLoanable() {
        this.loanable = false;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getPublisher() {
        return publisher;
    }

    public Integer getPublishedYear() {
        return publishedYear;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public List<LibraryItem> getItems() {
        return items;
    }
}
