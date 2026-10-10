package com.example.library.presentation.controller;

import com.example.library.application.Operator;
import com.example.library.application.OperatorRole;
import com.example.library.application.ReaderAppService;
import com.example.library.presentation.dto.ApiResponse;
import com.example.library.presentation.dto.ReaderResponse;
import com.example.library.presentation.dto.RegisterReaderRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 读者接口：注册（FR-001）与查询本人借阅（FR-013）。Controller 只做参数校验、权限信息透传与响应封装。
 */
@RestController
@RequestMapping("/api")
public class ReaderController {

    private final ReaderAppService readerAppService;

    public ReaderController(ReaderAppService readerAppService) {
        this.readerAppService = readerAppService;
    }

    @PostMapping("/readers")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterReaderRequest request) {
        ReaderResponse response = readerAppService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("读者注册成功", response));
    }

    @GetMapping("/readers/{readerId}/loans")
    public ApiResponse listLoans(@PathVariable Long readerId,
                                 @RequestHeader(value = "X-Operator-Id", required = false) Long operatorId,
                                 @RequestHeader(value = "X-Operator-Role", required = false) OperatorRole operatorRole) {
        return ApiResponse.ok(readerAppService.listLoans(readerId, Operator.of(operatorId, operatorRole), LocalDate.now()));
    }
}
