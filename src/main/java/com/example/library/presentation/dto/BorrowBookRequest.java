package com.example.library.presentation.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 办理借书请求（FR-011）。
 */
public record BorrowBookRequest(
        @NotBlank(message = "借阅证号不能为空") String cardNo,
        @NotBlank(message = "馆藏条码不能为空") String barcode) {
}
