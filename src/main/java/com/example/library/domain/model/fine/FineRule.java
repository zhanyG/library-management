package com.example.library.domain.model.fine;

import com.example.library.domain.model.catalog.ItemType;
import com.example.library.exception.InvalidPolicyValueException;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 罚款规则（策略对象）：按借出物类型定义免罚期、每日罚款与单笔上限（BR-008、BR-009）。
 */
@Entity
@Table(name = "fine_rules")
public class FineRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, unique = true, length = 32)
    private ItemType itemType;

    @Column(name = "grace_period_days", nullable = false)
    private int gracePeriodDays;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "daily_fine", nullable = false, precision = 10, scale = 2)),
            @AttributeOverride(name = "currency", column = @Column(name = "daily_fine_currency", nullable = false, length = 8))
    })
    private Money dailyFine;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "max_fine", nullable = false, precision = 10, scale = 2)),
            @AttributeOverride(name = "currency", column = @Column(name = "max_fine_currency", nullable = false, length = 8))
    })
    private Money maxFine;

    protected FineRule() {
    }

    public FineRule(ItemType itemType, int gracePeriodDays, Money dailyFine, Money maxFine) {
        this.itemType = itemType;
        this.gracePeriodDays = gracePeriodDays;
        this.dailyFine = dailyFine;
        this.maxFine = maxFine;
        validate();
    }

    /** 计入罚款的超期天数：扣除免罚期，负数记 0（BR-008）。 */
    public int chargeableOverdueDays(int rawOverdueDays) {
        int days = rawOverdueDays - gracePeriodDays;
        return Math.max(days, 0);
    }

    /** 计算罚款：超期天数 × 每日金额，且不超过单笔上限（BR-009）。 */
    public Money calcFine(int rawOverdueDays) {
        int days = chargeableOverdueDays(rawOverdueDays);
        if (days <= 0) {
            return Money.zero();
        }
        return dailyFine.times(days).cappedAt(maxFine);
    }

    /** 规则维护校验（FR-016）。 */
    public void update(int gracePeriodDays, Money dailyFine, Money maxFine) {
        this.gracePeriodDays = gracePeriodDays;
        this.dailyFine = dailyFine;
        this.maxFine = maxFine;
        validate();
    }

    private void validate() {
        if (gracePeriodDays < 0) {
            throw new InvalidPolicyValueException("免罚期不能为负数");
        }
        if (dailyFine == null || dailyFine.amount().signum() < 0) {
            throw new InvalidPolicyValueException("每日罚款不能为负");
        }
        if (maxFine == null || maxFine.amount().signum() < 0) {
            throw new InvalidPolicyValueException("罚款上限不能为负");
        }
    }

    public Long getId() {
        return id;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public int getGracePeriodDays() {
        return gracePeriodDays;
    }

    public Money getDailyFine() {
        return dailyFine;
    }

    public Money getMaxFine() {
        return maxFine;
    }
}
