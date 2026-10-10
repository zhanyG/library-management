package com.example.library.presentation.dto;

/**
 * 统一成功响应体。
 */
public record ApiResponse(String code, String message, Object data) {

    public static ApiResponse ok(Object data) {
        return new ApiResponse("OK", "success", data);
    }

    public static ApiResponse ok(String message, Object data) {
        return new ApiResponse("OK", message, data);
    }
}
