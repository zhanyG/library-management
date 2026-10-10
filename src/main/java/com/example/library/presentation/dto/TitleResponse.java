package com.example.library.presentation.dto;

/**
 * 图书标题响应。
 */
public record TitleResponse(Long titleId, String name, String author, String isbn,
                            String itemType, boolean loanable, long totalItems, long availableItems) {
}
