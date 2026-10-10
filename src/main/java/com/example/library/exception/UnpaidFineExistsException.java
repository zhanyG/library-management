package com.example.library.exception;

/**
 * 存在未缴罚款（错误码 READER_HAS_UNPAID_FINE）。
 */
public class UnpaidFineExistsException extends BusinessException {

    public UnpaidFineExistsException(String message) {
        super("READER_HAS_UNPAID_FINE", message);
    }

    public UnpaidFineExistsException() {
        super("READER_HAS_UNPAID_FINE", "存在未缴罚款");
    }
}
