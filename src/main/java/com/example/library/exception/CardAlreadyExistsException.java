package com.example.library.exception;

/**
 * 该读者已持有有效借阅证（错误码 CARD_ALREADY_EXISTS）。
 */
public class CardAlreadyExistsException extends BusinessException {

    public CardAlreadyExistsException(String message) {
        super("CARD_ALREADY_EXISTS", message);
    }

    public CardAlreadyExistsException() {
        super("CARD_ALREADY_EXISTS", "该读者已持有有效借阅证");
    }
}
