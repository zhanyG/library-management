package com.example.library.domain.service;

import com.example.library.domain.model.circulation.Loan;
import com.example.library.domain.model.fine.FineRecord;
import com.example.library.domain.model.fine.FineRule;
import com.example.library.domain.model.fine.Money;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 罚款计算领域服务（UC-210 / BR-008 / BR-009）：按借出物类型的罚款规则计算金额。
 */
@Service
public class FineCalculator {

    /**
     * 计算超期罚款。
     *
     * @return 需缴纳的罚款记录；未超期或免罚期内返回空
     */
    public Optional<FineRecord> calculate(Loan loan, FineRule fineRule, int overdueDays) {
        Money amount = fineRule.calcFine(overdueDays);
        if (amount.isZero()) {
            return Optional.empty();
        }
        return Optional.of(new FineRecord(loan.getId(), loan.getReaderId(), amount));
    }
}
