package com.example.library.presentation.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 预约图书请求（FR-014）。
 */
public record ReserveBookRequest(
        @NotNull(message = "读者 ID 不能为空") Long readerId,
        @NotNull(message = "题名 ID 不能为空") Long titleId) {
}
