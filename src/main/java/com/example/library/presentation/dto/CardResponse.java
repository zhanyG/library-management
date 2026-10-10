package com.example.library.presentation.dto;

import java.time.LocalDate;

/**
 * 借阅证响应。
 */
public record CardResponse(String cardNo, String ownerName, String department, LocalDate issueDate, String status) {
}
