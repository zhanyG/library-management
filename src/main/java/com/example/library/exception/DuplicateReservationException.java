package com.example.library.exception;

/**
 * 已存在同一题名的有效预约（错误码 DUPLICATE_RESERVATION）。
 */
public class DuplicateReservationException extends BusinessException {

    public DuplicateReservationException(String message) {
        super("DUPLICATE_RESERVATION", message);
    }

    public DuplicateReservationException() {
        super("DUPLICATE_RESERVATION", "已存在同一题名的有效预约");
    }
}
