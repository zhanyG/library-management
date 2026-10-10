package com.example.library.application;

/**
 * 操作人：由展示层从请求头解析后传入应用服务。
 */
public record Operator(Long id, OperatorRole role) {

    public static Operator of(Long id, OperatorRole role) {
        return new Operator(id, role);
    }

    public static Operator librarian(Long id) {
        return new Operator(id, OperatorRole.LIBRARIAN);
    }

    public static Operator admin(Long id) {
        return new Operator(id, OperatorRole.SYSTEM_ADMIN);
    }

    public static Operator reader(Long id) {
        return new Operator(id, OperatorRole.READER);
    }

    public String roleName() {
        return role == null ? "ANONYMOUS" : role.name();
    }
}
