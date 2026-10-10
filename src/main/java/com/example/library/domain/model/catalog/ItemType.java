package com.example.library.domain.model.catalog;

/**
 * 借出物类型：决定罚款规则（FineRule）与可外借性。
 */
public enum ItemType {

    CHINESE_BOOK("中文图书", true),
    FOREIGN_BOOK("外文图书", true),
    CHINESE_MAGAZINE("中文杂志", true),
    FOREIGN_MAGAZINE("外文杂志", true),
    THESIS("论文", false);

    private final String displayName;
    private final boolean loanableByDefault;

    ItemType(String displayName, boolean loanableByDefault) {
        this.displayName = displayName;
        this.loanableByDefault = loanableByDefault;
    }

    public String displayName() {
        return displayName;
    }

    /** 该类型是否默认可外借（论文默认仅馆内阅览）。 */
    public boolean loanableByDefault() {
        return loanableByDefault;
    }
}
