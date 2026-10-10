package com.example.library.presentation.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 办理借阅证请求（FR-002）。
 */
public record IssueCardRequest(@NotNull(message = "读者 ID 不能为空") Long readerId) {
}
