package com.example.library.presentation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 添加馆藏副本请求（FR-008）。
 */
public record AddItemRequest(
        @NotNull(message = "题名 ID 不能为空") Long titleId,
        @Size(max = 64) String location) {
}
