package com.example.library.exception;

/**
 * 该类型借出物不可外借（错误码 ITEM_NOT_LOANABLE）。
 */
public class ItemNotLoanableException extends BusinessException {

    public ItemNotLoanableException(String message) {
        super("ITEM_NOT_LOANABLE", message);
    }

    public ItemNotLoanableException() {
        super("ITEM_NOT_LOANABLE", "该类型借出物不可外借");
    }
}
