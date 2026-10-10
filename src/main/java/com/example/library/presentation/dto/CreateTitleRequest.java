package com.example.library.presentation.dto;

import com.example.library.domain.model.catalog.ItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 添加图书标题请求（FR-006）。
 */
public record CreateTitleRequest(
        @NotBlank(message = "题名不能为空") @Size(max = 200) String name,
        @Size(max = 120) String author,
        @NotBlank(message = "ISBN 不能为空") @Size(max = 32) String isbn,
        @Size(max = 120) String publisher,
        Integer publishedYear,
        @NotNull(message = "借出物类型不能为空") ItemType itemType) {
}
