package com.example.library.exception;

/**
 * 超过该读者类型可借数量上限（错误码 QUOTA_EXCEEDED）。
 */
public class QuotaExceededException extends BusinessException {

    public QuotaExceededException(String message) {
        super("QUOTA_EXCEEDED", message);
    }

    public QuotaExceededException() {
        super("QUOTA_EXCEEDED", "超过该读者类型可借数量上限");
    }
}
