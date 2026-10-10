package com.example.library.exception;

/**
 * 规则数值非法（错误码 INVALID_POLICY_VALUE）。
 */
public class InvalidPolicyValueException extends BusinessException {

    public InvalidPolicyValueException(String message) {
        super("INVALID_POLICY_VALUE", message);
    }

    public InvalidPolicyValueException() {
        super("INVALID_POLICY_VALUE", "规则数值非法");
    }
}
