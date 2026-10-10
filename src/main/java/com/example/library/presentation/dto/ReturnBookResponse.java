package com.example.library.presentation.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 还书结果响应（含超期与罚款信息）。
 */
public record ReturnBookResponse(Long loanId, LocalDate returnDate, int overdueDays,
                                 BigDecimal fineAmount, String fineStatus) {
}
