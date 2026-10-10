package com.example.library.exception;

/**
 * 该 ISBN 已存在（错误码 TITLE_ALREADY_EXISTS）。
 */
public class TitleAlreadyExistsException extends BusinessException {

    public TitleAlreadyExistsException(String message) {
        super("TITLE_ALREADY_EXISTS", message);
    }

    public TitleAlreadyExistsException() {
        super("TITLE_ALREADY_EXISTS", "该 ISBN 已存在");
    }
}
