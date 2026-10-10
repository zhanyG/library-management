package com.example.library.exception;

/**
 * 条码已存在（错误码 DUPLICATE_BARCODE）。
 */
public class DuplicateBarcodeException extends BusinessException {

    public DuplicateBarcodeException(String message) {
        super("DUPLICATE_BARCODE", message);
    }

    public DuplicateBarcodeException() {
        super("DUPLICATE_BARCODE", "条码已存在");
    }
}
