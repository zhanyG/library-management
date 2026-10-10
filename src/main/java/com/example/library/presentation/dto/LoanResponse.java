package com.example.library.presentation.dto;

import java.time.LocalDate;

/**
 * 借书成功响应。
 */
public record LoanResponse(Long loanId, String readerName, String titleName, String barcode,
                           LocalDate borrowDate, LocalDate dueDate, String status) {
}
