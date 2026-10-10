package com.example.library.exception;

/**
 * 馆藏副本已被他人预约（错误码 ITEM_RESERVED_BY_OTHER）。
 */
public class ItemReservedByOtherException extends BusinessException {

    public ItemReservedByOtherException(String message) {
        super("ITEM_RESERVED_BY_OTHER", message);
    }

    public ItemReservedByOtherException() {
        super("ITEM_RESERVED_BY_OTHER", "馆藏副本已被他人预约");
    }
}
