package com.example.library.exception;

/**
 * 馆藏副本不可借（错误码 ITEM_NOT_AVAILABLE）。
 */
public class ItemNotAvailableException extends BusinessException {

    public ItemNotAvailableException(String message) {
        super("ITEM_NOT_AVAILABLE", message);
    }

    public ItemNotAvailableException() {
        super("ITEM_NOT_AVAILABLE", "馆藏副本不可借");
    }
}
