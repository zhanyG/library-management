package com.example.library.presentation.dto;

import java.time.LocalDate;

/**
 * 借阅信息响应（FR-013）。
 */
public record LoanQueryResponse(Long loanId, String titleName, String barcode,
                                LocalDate borrowDate, LocalDate dueDate, LocalDate returnDate,
                                String status, boolean overdue, int overdueDays) {
}
