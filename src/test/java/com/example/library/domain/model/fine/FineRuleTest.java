package com.example.library.domain.model.fine;

import com.example.library.domain.model.catalog.ItemType;
import com.example.library.exception.InvalidPolicyValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TC-029 FineRule 罚款规则单元测试（BR-008、BR-009）。
 */
class FineRuleTest {

    private FineRule chineseBook() {
        return new FineRule(ItemType.CHINESE_BOOK, 3, Money.of("0.20"), Money.of("50.00"));
    }

    @Test
    @DisplayName("未超期或免罚期内罚款为 0")
    void noFineWithinGracePeriod() {
        FineRule rule = chineseBook();

        assertThat(rule.chargeableOverdueDays(0)).isZero();
        assertThat(rule.calcFine(0).isZero()).isTrue();
        assertThat(rule.calcFine(3).isZero()).isTrue();
        assertThat(rule.calcFine(-2).isZero()).isTrue();
    }

    @Test
    @DisplayName("超期天数扣除免罚期后乘以每日金额")
    void fineBeyondGracePeriod() {
        FineRule rule = chineseBook();

        // 超期 20 天，免罚 3 天 → 17 天 × 0.20 = 3.40
        assertThat(rule.chargeableOverdueDays(20)).isEqualTo(17);
        assertThat(rule.calcFine(20)).isEqualTo(Money.of("3.40"));
    }

    @Test
    @DisplayName("罚款受单笔上限约束")
    void fineCappedAtMax() {
        FineRule magazine = new FineRule(ItemType.CHINESE_MAGAZINE, 1, Money.of("0.50"), Money.of("20.00"));

        assertThat(magazine.calcFine(400)).isEqualTo(Money.of("20.00"));
    }

    @Test
    @DisplayName("各借出物类型规则独立：外文图书 5/0.50/100，论文 0/2.00/100")
    void perItemTypeRules() {
        FineRule foreignBook = new FineRule(ItemType.FOREIGN_BOOK, 5, Money.of("0.50"), Money.of("100.00"));
        FineRule thesis = new FineRule(ItemType.THESIS, 0, Money.of("2.00"), Money.of("100.00"));

        assertThat(foreignBook.calcFine(10)).isEqualTo(Money.of("2.50"));
        assertThat(thesis.calcFine(5)).isEqualTo(Money.of("10.00"));
        assertThat(thesis.calcFine(70)).isEqualTo(Money.of("100.00"));
    }

    @Test
    @DisplayName("规则维护：负数或空值被拒绝（FR-016）")
    void rejectInvalidRuleValues() {
        assertThatThrownBy(() -> new FineRule(ItemType.CHINESE_BOOK, -1, Money.of("0.20"), Money.of("50.00")))
                .isInstanceOf(InvalidPolicyValueException.class);
        assertThatThrownBy(() -> new FineRule(ItemType.CHINESE_BOOK, 3, null, Money.of("50.00")))
                .isInstanceOf(InvalidPolicyValueException.class);
        assertThatThrownBy(() -> new FineRule(ItemType.CHINESE_BOOK, 3, Money.of("0.20"), null))
                .isInstanceOf(InvalidPolicyValueException.class);
    }
}
