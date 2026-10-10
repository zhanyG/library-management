package com.example.library.domain.model.circulation;

import com.example.library.domain.model.reader.ReaderType;
import com.example.library.exception.InvalidPolicyValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TC-030 BorrowPolicy 借阅规则单元测试（BR-003、BR-007）。
 */
class BorrowPolicyTest {

    @Test
    @DisplayName("本科生：最多 5 本，期限 30 天")
    void undergraduatePolicy() {
        BorrowPolicy policy = new BorrowPolicy(ReaderType.UNDERGRADUATE, 5, 30);

        assertThat(policy.getMaxLoanCount()).isEqualTo(5);
        assertThat(policy.exceedQuota(4)).isFalse();
        assertThat(policy.exceedQuota(5)).isTrue();
        assertThat(policy.calcDueDate(LocalDate.of(2026, 10, 1))).isEqualTo(LocalDate.of(2026, 10, 31));
    }

    @Test
    @DisplayName("研究生 10/60、博士生 15/60、教师 20/90")
    void otherPolicies() {
        BorrowPolicy postgraduate = new BorrowPolicy(ReaderType.POSTGRADUATE, 10, 60);
        BorrowPolicy doctoral = new BorrowPolicy(ReaderType.DOCTORAL, 15, 60);
        BorrowPolicy teacher = new BorrowPolicy(ReaderType.TEACHER, 20, 90);

        assertThat(postgraduate.exceedQuota(9)).isFalse();
        assertThat(postgraduate.exceedQuota(10)).isTrue();
        assertThat(doctoral.getMaxLoanCount()).isEqualTo(15);
        assertThat(teacher.getLoanPeriodDays()).isEqualTo(90);
        assertThat(teacher.calcDueDate(LocalDate.of(2026, 1, 1))).isEqualTo(LocalDate.of(2026, 4, 1));
    }

    @Test
    @DisplayName("规则维护：非法数值被拒绝（FR-016）")
    void rejectInvalidValues() {
        assertThatThrownBy(() -> new BorrowPolicy(ReaderType.UNDERGRADUATE, 0, 30))
                .isInstanceOf(InvalidPolicyValueException.class);
        assertThatThrownBy(() -> new BorrowPolicy(ReaderType.UNDERGRADUATE, 5, -1))
                .isInstanceOf(InvalidPolicyValueException.class);

        BorrowPolicy policy = new BorrowPolicy(ReaderType.UNDERGRADUATE, 5, 30);
        assertThatThrownBy(() -> policy.update(-1, 30))
                .isInstanceOf(InvalidPolicyValueException.class);
    }

    @Test
    @DisplayName("规则更新后立即生效")
    void updateTakesEffect() {
        BorrowPolicy policy = new BorrowPolicy(ReaderType.UNDERGRADUATE, 5, 30);

        policy.update(6, 45);

        assertThat(policy.exceedQuota(5)).isFalse();
        assertThat(policy.exceedQuota(6)).isTrue();
        assertThat(policy.calcDueDate(LocalDate.of(2026, 10, 1))).isEqualTo(LocalDate.of(2026, 11, 15));
    }
}
