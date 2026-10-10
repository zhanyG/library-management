package com.example.library.exception;

/**
 * 副本在借，不能删除（错误码 ITEM_ON_LOAN_CANNOT_DELETE）。
 */
public class ItemOnLoanCannotDeleteException extends BusinessException {

    public ItemOnLoanCannotDeleteException(String message) {
        super("ITEM_ON_LOAN_CANNOT_DELETE", message);
    }

    public ItemOnLoanCannotDeleteException() {
        super("ITEM_ON_LOAN_CANNOT_DELETE", "副本在借，不能删除");
    }
}
