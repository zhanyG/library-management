package com.example.library.exception;

/**
 * 读者不存在（错误码 READER_NOT_FOUND）。
 */
public class ReaderNotFoundException extends BusinessException {

    public ReaderNotFoundException(String message) {
        super("READER_NOT_FOUND", message);
    }

    public ReaderNotFoundException() {
        super("READER_NOT_FOUND", "读者不存在");
    }
}
