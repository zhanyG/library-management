package com.example.library.exception;

/**
 * 不是本馆藏书（错误码 ITEM_NOT_FOUND）。
 */
public class ItemNotFoundException extends BusinessException {

    public ItemNotFoundException(String message) {
        super("ITEM_NOT_FOUND", message);
    }

    public ItemNotFoundException() {
        super("ITEM_NOT_FOUND", "不是本馆藏书");
    }
}
