package com.example.library.exception;

/**
 * 图书标题不存在（错误码 TITLE_NOT_FOUND）。
 */
public class TitleNotFoundException extends BusinessException {

    public TitleNotFoundException(String message) {
        super("TITLE_NOT_FOUND", message);
    }

    public TitleNotFoundException() {
        super("TITLE_NOT_FOUND", "图书标题不存在");
    }
}
