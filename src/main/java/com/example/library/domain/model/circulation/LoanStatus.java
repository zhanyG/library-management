package com.example.library.domain.model.circulation;

/**
 * 借阅记录状态：借出、超期、已归还。
 */
public enum LoanStatus {
    BORROWED,
    OVERDUE,
    RETURNED
}
