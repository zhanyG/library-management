package com.example.library.presentation.dto;

import com.example.library.domain.model.reader.ReaderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 注册读者请求（FR-001）。
 */
public record RegisterReaderRequest(
        @NotBlank(message = "姓名不能为空") @Size(max = 64) String name,
        @NotBlank(message = "证件号不能为空") @Size(max = 32) String idCardNo,
        @Size(max = 20) String phone,
        @NotBlank(message = "院系或单位不能为空") @Size(max = 64) String department,
        @NotNull(message = "读者类型不能为空") ReaderType readerType,
        @Size(max = 32) String studentNo,
        @Size(max = 16) String grade,
        @Size(max = 64) String major,
        @Size(max = 32) String employeeNo,
        @Size(max = 32) String title,
        @Size(max = 64) String faculty) {
}
