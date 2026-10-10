package com.example.library.infrastructure.persistence;

import com.example.library.domain.model.catalog.ItemType;
import com.example.library.domain.model.fine.FineRule;
import com.example.library.domain.model.fine.Money;
import com.example.library.domain.repository.FineRuleRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 罚款规则初始化器：按 specs/01-clarifying-questions.md Q19 写入各借出物类型的免罚期、每日罚款与上限。
 *
 * <p>仅在规则表为空时写入，避免覆盖系统管理员维护后的规则。</p>
 */
@Component
@Order(2)
public class FineRuleInitializer implements ApplicationRunner {

    private final FineRuleRepository fineRuleRepository;

    public FineRuleInitializer(FineRuleRepository fineRuleRepository) {
        this.fineRuleRepository = fineRuleRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!fineRuleRepository.findAll().isEmpty()) {
            return;
        }
        List<FineRule> defaults = List.of(
                new FineRule(ItemType.CHINESE_BOOK, 3, Money.of("0.20"), Money.of("50.00")),
                new FineRule(ItemType.FOREIGN_BOOK, 5, Money.of("0.50"), Money.of("100.00")),
                new FineRule(ItemType.CHINESE_MAGAZINE, 1, Money.of("0.50"), Money.of("20.00")),
                new FineRule(ItemType.FOREIGN_MAGAZINE, 2, Money.of("1.00"), Money.of("50.00")),
                new FineRule(ItemType.THESIS, 0, Money.of("2.00"), Money.of("100.00")));
        defaults.forEach(fineRuleRepository::save);
    }
}
