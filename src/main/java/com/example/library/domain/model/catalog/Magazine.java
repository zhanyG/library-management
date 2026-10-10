package com.example.library.domain.model.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * 杂志副本。
 */
@Entity
@DiscriminatorValue("MAGAZINE")
public class Magazine extends LibraryItem {

    @Column(name = "language", length = 16)
    private String language;

    @Column(name = "issue", length = 32)
    private String issue;

    @Column(name = "periodical_no", length = 32)
    private String periodicalNo;

    protected Magazine() {
    }

    public Magazine(String barcode, ItemType itemType, String location, String language,
                    String issue, String periodicalNo) {
        super(barcode, itemType, location);
        this.language = language;
        this.issue = issue;
        this.periodicalNo = periodicalNo;
    }

    public String getLanguage() {
        return language;
    }

    public String getIssue() {
        return issue;
    }

    public String getPeriodicalNo() {
        return periodicalNo;
    }
}
