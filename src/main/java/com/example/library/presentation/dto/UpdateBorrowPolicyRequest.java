package com.example.library.presentation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 维护借阅规则请求（FR-016）。
 */
public record UpdateBorrowPolicyRequest(
        @NotNull(message = "最大借阅数量不能为空") @Positive(message = "最大借阅数量必须大于 0") Integer maxLoanCount,
        @NotNull(message = "借阅期限不能为空") @Positive(message = "借阅期限必须大于 0") Integer loanPeriodDays) {
}
