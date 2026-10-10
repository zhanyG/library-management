package com.example.library.exception;

import com.example.library.presentation.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 全局异常处理器：把业务异常转换为统一错误响应（不把业务规则写在这一层）。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 需要返回 409 Conflict 的错误码。 */
    private static final Set<String> CONFLICT_CODES = Set.of(
            "CARD_ALREADY_EXISTS", "CARD_HAS_ACTIVE_LOANS",
            "TITLE_ALREADY_EXISTS", "TITLE_HAS_ACTIVE_LOANS",
            "DUPLICATE_RESERVATION", "RESERVATION_LIMIT_EXCEEDED",
            "DUPLICATE_BARCODE", "ITEM_ON_LOAN_CANNOT_DELETE");

    /** 需要返回 404 Not Found 的错误码。 */
    private static final Set<String> NOT_FOUND_CODES = Set.of(
            "READER_NOT_FOUND", "TITLE_NOT_FOUND", "LIBRARIAN_NOT_FOUND",
            "ITEM_NOT_FOUND", "RESERVATION_NOT_FOUND");

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest request) {
        HttpStatus status = resolveStatus(ex.getCode());
        return ResponseEntity.status(status)
                .body(new ErrorResponse(ex.getCode(), ex.getMessage(), traceId(request)));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("VALIDATION_ERROR", message, traceId(request)));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex,
                                                               HttpServletRequest request) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("INVALID_ARGUMENT", ex.getMessage(), traceId(request)));
    }

    private HttpStatus resolveStatus(String code) {
        if (code == null) {
            return HttpStatus.BAD_REQUEST;
        }
        if ("PERMISSION_DENIED".equals(code)) {
            return HttpStatus.FORBIDDEN;
        }
        if (NOT_FOUND_CODES.contains(code)) {
            return HttpStatus.NOT_FOUND;
        }
        if (CONFLICT_CODES.contains(code)) {
            return HttpStatus.CONFLICT;
        }
        if ("INVALID_POLICY_VALUE".equals(code)) {
            return HttpStatus.UNPROCESSABLE_ENTITY;
        }
        return HttpStatus.BAD_REQUEST;
    }

    private String traceId(HttpServletRequest request) {
        String traceId = request.getHeader("X-Trace-Id");
        return traceId == null ? UUID.randomUUID().toString() : traceId;
    }
}
