package com.example.library.exception;

/**
 * 借阅证不存在或已失效（错误码 CARD_INVALID）。
 */
public class CardInvalidException extends BusinessException {

    public CardInvalidException(String message) {
        super("CARD_INVALID", message);
    }

    public CardInvalidException() {
        super("CARD_INVALID", "借阅证不存在或已失效");
    }
}
