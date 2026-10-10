package com.example.library.presentation.controller;

import com.example.library.application.CirculationAppService;
import com.example.library.application.Operator;
import com.example.library.application.OperatorRole;
import com.example.library.presentation.dto.ApiResponse;
import com.example.library.presentation.dto.BorrowBookRequest;
import com.example.library.presentation.dto.ReturnBookRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 流通接口：办理借书、办理还书、按借阅证查询（仅图书管理员）。
 */
@RestController
@RequestMapping("/api/circulation")
public class CirculationController {

    private final CirculationAppService circulationAppService;

    public CirculationController(CirculationAppService circulationAppService) {
        this.circulationAppService = circulationAppService;
    }

    @PostMapping("/borrow")
    public ApiResponse borrow(@Valid @RequestBody BorrowBookRequest request,
                              @RequestHeader(value = "X-Operator-Id", required = false) Long operatorId,
                              @RequestHeader(value = "X-Operator-Role", required = false) OperatorRole operatorRole) {
        return ApiResponse.ok("借书成功",
                circulationAppService.borrowBook(request, Operator.of(operatorId, operatorRole)));
    }

    @PostMapping("/return")
    public ApiResponse returnBook(@Valid @RequestBody ReturnBookRequest request,
                                  @RequestHeader(value = "X-Operator-Id", required = false) Long operatorId,
                                  @RequestHeader(value = "X-Operator-Role", required = false) OperatorRole operatorRole) {
        return ApiResponse.ok("还书成功",
                circulationAppService.returnBook(request, Operator.of(operatorId, operatorRole)));
    }

    @GetMapping("/readers/{cardNo}/loans")
    public ApiResponse listLoansByCard(@PathVariable String cardNo,
                                       @RequestHeader(value = "X-Operator-Id", required = false) Long operatorId,
                                       @RequestHeader(value = "X-Operator-Role", required = false) OperatorRole operatorRole) {
        return ApiResponse.ok(circulationAppService.listLoansByCard(cardNo, Operator.of(operatorId, operatorRole)));
    }
}
