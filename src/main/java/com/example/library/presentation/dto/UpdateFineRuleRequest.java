package com.example.library.presentation.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 维护罚款规则请求（FR-016）。
 *
 * <p>数值合法性由领域实体 FineRule 校验并返回 422 INVALID_POLICY_VALUE，本 DTO 只负责非空校验。</p>
 */
public record UpdateFineRuleRequest(
        @NotNull(message = "免罚期不能为空") Integer gracePeriodDays,
        @NotNull(message = "每日罚款不能为空") BigDecimal dailyFine,
        @NotNull(message = "罚款上限不能为空") BigDecimal maxFine) {
}
