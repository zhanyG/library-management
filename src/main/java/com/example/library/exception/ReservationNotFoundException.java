package com.example.library.exception;

/**
 * 预约记录不存在（错误码 RESERVATION_NOT_FOUND）。
 */
public class ReservationNotFoundException extends BusinessException {

    public ReservationNotFoundException(String message) {
        super("RESERVATION_NOT_FOUND", message);
    }

    public ReservationNotFoundException() {
        super("RESERVATION_NOT_FOUND", "预约记录不存在");
    }
}
