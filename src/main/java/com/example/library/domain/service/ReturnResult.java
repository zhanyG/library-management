package com.example.library.domain.service;

import com.example.library.domain.model.fine.Money;

import java.time.LocalDate;

/**
 * 还书结果（UC-102）：借阅记录、归还日期、超期天数与罚款信息。
 */
public record ReturnResult(Long loanId, LocalDate returnDate, int overdueDays, Money fineAmount, String fineStatus) {
}
