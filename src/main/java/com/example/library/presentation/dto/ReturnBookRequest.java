package com.example.library.presentation.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 办理还书请求（FR-012）。
 */
public record ReturnBookRequest(@NotBlank(message = "馆藏条码不能为空") String barcode) {
}
