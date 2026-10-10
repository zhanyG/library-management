package com.example.library.presentation.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * 维护罚款规则请求（FR-016）。
 */
public record UpdateFineRuleRequest(
        @NotNull(message = "免罚期不能为空") @PositiveOrZero(message = "免罚期不能为负") Integer gracePeriodDays,
        @NotNull(message = "每日罚款不能为空") @DecimalMin(value = "0.00", message = "每日罚款不能为负") BigDecimal dailyFine,
        @NotNull(message = "罚款上限不能为空") @DecimalMin(value = "0.00", message = "罚款上限不能为负") BigDecimal maxFine) {
}
