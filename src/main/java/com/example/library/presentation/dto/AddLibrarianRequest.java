package com.example.library.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 添加图书管理员请求（FR-004）。
 */
public record AddLibrarianRequest(
        @NotBlank(message = "工号不能为空") @Size(max = 32) String staffNo,
        @NotBlank(message = "姓名不能为空") @Size(max = 64) String name) {
}
