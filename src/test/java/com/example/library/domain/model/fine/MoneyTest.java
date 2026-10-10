package com.example.library.domain.model.fine;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TC-031 Money 值对象单元测试：金额计算必须无浮点误差。
 */
class MoneyTest {

    @Test
    @DisplayName("加法使用 BigDecimal，无浮点误差")
    void plusUsesBigDecimal() {
        Money result = Money.of("0.1").plus(Money.of("0.2"));

        assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("0.30"));
        assertThat(result).isEqualTo(Money.of("0.30"));
    }

    @Test
    @DisplayName("乘以天数后保留两位小数")
    void timesKeepsScale() {
        assertThat(Money.of("0.20").times(17).amount()).isEqualByComparingTo(new BigDecimal("3.40"));
    }

    @Test
    @DisplayName("超过上限时截断为上限（BR-009）")
    void cappedAtUpperBound() {
        Money capped = Money.of("200.00").cappedAt(Money.of("50.00"));

        assertThat(capped).isEqualTo(Money.of("50.00"));
        assertThat(capped.isGreaterThan(Money.of("50.00"))).isFalse();
    }

    @Test
    @DisplayName("零金额判定")
    void zeroAmount() {
        assertThat(Money.zero().isZero()).isTrue();
        assertThat(Money.of("0.01").isZero()).isFalse();
    }

    @Test
    @DisplayName("金额值相等语义（不同标度视为相等）")
    void valueEquality() {
        assertThat(Money.of("3.4")).isEqualTo(Money.of("3.40"));
        assertThat(Money.of("3.4")).hasSameHashCodeAs(Money.of("3.40"));
        assertThat(Money.of("3.40")).isNotEqualTo(Money.of("3.41"));
    }
}
