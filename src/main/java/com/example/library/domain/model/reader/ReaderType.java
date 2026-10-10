package com.example.library.domain.model.reader;

/**
 * 读者类型（BR-001~BR-003 的规则主体，具体借阅数量与期限由 BorrowPolicy 维护）。
 */
public enum ReaderType {

    JUNIOR_COLLEGE("专科生"),
    UNDERGRADUATE("本科生"),
    POSTGRADUATE("研究生"),
    DOCTORAL("博士生"),
    TEACHER("教师");

    private final String displayName;

    ReaderType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
