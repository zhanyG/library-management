package com.example.library.exception;

/**
 * 业务异常基类：携带统一错误码，由 GlobalExceptionHandler 转换为标准响应。
 */
public class BusinessException extends RuntimeException {

    private final String code;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
