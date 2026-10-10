package com.example.library.exception;

/**
 * 有效预约数量已达上限（错误码 RESERVATION_LIMIT_EXCEEDED）。
 */
public class ReservationLimitExceededException extends BusinessException {

    public ReservationLimitExceededException(String message) {
        super("RESERVATION_LIMIT_EXCEEDED", message);
    }

    public ReservationLimitExceededException() {
        super("RESERVATION_LIMIT_EXCEEDED", "有效预约数量已达上限");
    }
}
