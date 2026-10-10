package com.example.library.exception;

/**
 * 图书管理员不存在（错误码 LIBRARIAN_NOT_FOUND）。
 */
public class LibrarianNotFoundException extends BusinessException {

    public LibrarianNotFoundException(String message) {
        super("LIBRARIAN_NOT_FOUND", message);
    }

    public LibrarianNotFoundException() {
        super("LIBRARIAN_NOT_FOUND", "图书管理员不存在");
    }
}
