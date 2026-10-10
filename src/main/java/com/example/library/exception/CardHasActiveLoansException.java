package com.example.library.exception;

/**
 * 存在未还借阅记录，不能注销借阅证（错误码 CARD_HAS_ACTIVE_LOANS）。
 */
public class CardHasActiveLoansException extends BusinessException {

    public CardHasActiveLoansException(String message) {
        super("CARD_HAS_ACTIVE_LOANS", message);
    }

    public CardHasActiveLoansException() {
        super("CARD_HAS_ACTIVE_LOANS", "存在未还借阅记录，不能注销借阅证");
    }
}
