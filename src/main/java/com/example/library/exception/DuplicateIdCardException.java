package com.example.library.exception;

/**
 * 该证件号已注册（错误码 DUPLICATE_ID_CARD）。
 */
public class DuplicateIdCardException extends BusinessException {

    public DuplicateIdCardException(String message) {
        super("DUPLICATE_ID_CARD", message);
    }

    public DuplicateIdCardException() {
        super("DUPLICATE_ID_CARD", "该证件号已注册");
    }
}
