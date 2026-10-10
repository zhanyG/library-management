package com.example.library.domain.model.reader;

/**
 * 借阅证状态（BR-002：REVOKED / LOST 状态不可借书）。
 */
public enum CardStatus {
    VALID,
    REVOKED,
    LOST
}
