package com.example.library.exception;

/**
 * 该副本当前不在借出状态（错误码 ITEM_NOT_ON_LOAN）。
 */
public class ItemNotOnLoanException extends BusinessException {

    public ItemNotOnLoanException(String message) {
        super("ITEM_NOT_ON_LOAN", message);
    }

    public ItemNotOnLoanException() {
        super("ITEM_NOT_ON_LOAN", "该副本当前不在借出状态");
    }
}
