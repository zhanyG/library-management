package com.example.library.presentation.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 罚款记录响应（FR-015）。
 */
public record FineResponse(Long fineId, Long loanId, Long readerId, BigDecimal amount,
                           String currency, String status, LocalDateTime createdAt) {
}
