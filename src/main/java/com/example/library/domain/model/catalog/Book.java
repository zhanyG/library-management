package com.example.library.domain.model.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * 图书副本（中文/外文由 {@link ItemType} 区分）。
 */
@Entity
@DiscriminatorValue("BOOK")
public class Book extends LibraryItem {

    @Column(name = "language", length = 16)
    private String language;

    @Column(name = "pages")
    private Integer pages;

    @Column(name = "edition", length = 32)
    private String edition;

    protected Book() {
    }

    public Book(String barcode, ItemType itemType, String location, String language, Integer pages, String edition) {
        super(barcode, itemType, location);
        this.language = language;
        this.pages = pages;
        this.edition = edition;
    }

    public String getLanguage() {
        return language;
    }

    public Integer getPages() {
        return pages;
    }

    public String getEdition() {
        return edition;
    }
}
