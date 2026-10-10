package com.example.library.exception;

/**
 * 当前角色无权执行该操作（错误码 PERMISSION_DENIED）。
 */
public class PermissionDeniedException extends BusinessException {

    public PermissionDeniedException(String message) {
        super("PERMISSION_DENIED", message);
    }

    public PermissionDeniedException() {
        super("PERMISSION_DENIED", "当前角色无权执行该操作");
    }
}
