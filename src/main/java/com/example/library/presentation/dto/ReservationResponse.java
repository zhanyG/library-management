package com.example.library.presentation.dto;

import java.time.LocalDateTime;

/**
 * 预约响应。
 */
public record ReservationResponse(Long reservationId, String titleName, int queuePosition,
                                  LocalDateTime createdAt, String status) {
}
