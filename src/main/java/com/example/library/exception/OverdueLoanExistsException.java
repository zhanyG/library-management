package com.example.library.exception;

/**
 * 存在超期未还图书（错误码 READER_HAS_OVERDUE_LOAN）。
 */
public class OverdueLoanExistsException extends BusinessException {

    public OverdueLoanExistsException(String message) {
        super("READER_HAS_OVERDUE_LOAN", message);
    }

    public OverdueLoanExistsException() {
        super("READER_HAS_OVERDUE_LOAN", "存在超期未还图书");
    }
}
