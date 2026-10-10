package com.example.library.presentation.dto;

/**
 * 统一失败响应体（07-architecture.md 第 5 节）。
 */
public record ErrorResponse(String code, String message, String traceId) {
}
