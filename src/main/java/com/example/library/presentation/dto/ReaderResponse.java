package com.example.library.presentation.dto;

/**
 * 读者信息响应。
 */
public record ReaderResponse(Long readerId, String name, String department, String readerType, String status) {
}
