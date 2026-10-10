package com.example.library.presentation.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 维护借阅规则请求（FR-016）。
 *
 * <p>数值合法性（必须大于 0）由领域实体 BorrowPolicy 校验并返回 422 INVALID_POLICY_VALUE，
 * 本 DTO 只负责非空校验。</p>
 */
public record UpdateBorrowPolicyRequest(
        @NotNull(message = "最大借阅数量不能为空") Integer maxLoanCount,
        @NotNull(message = "借阅期限不能为空") Integer loanPeriodDays) {
}
