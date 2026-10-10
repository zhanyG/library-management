package com.example.library.exception;

/**
 * 题名下存在在借副本，不能删除（错误码 TITLE_HAS_ACTIVE_LOANS）。
 */
public class TitleHasActiveLoansException extends BusinessException {

    public TitleHasActiveLoansException(String message) {
        super("TITLE_HAS_ACTIVE_LOANS", message);
    }

    public TitleHasActiveLoansException() {
        super("TITLE_HAS_ACTIVE_LOANS", "题名下存在在借副本，不能删除");
    }
}
