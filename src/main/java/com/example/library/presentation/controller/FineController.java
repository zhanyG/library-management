package com.example.library.presentation.controller;

import com.example.library.application.FineAppService;
import com.example.library.application.Operator;
import com.example.library.application.OperatorRole;
import com.example.library.presentation.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 罚款接口（FR-015）。
 */
@RestController
@RequestMapping("/api/fines")
public class FineController {

    private final FineAppService fineAppService;

    public FineController(FineAppService fineAppService) {
        this.fineAppService = fineAppService;
    }

    @GetMapping
    public ApiResponse list(@RequestParam("readerId") Long readerId,
                            @RequestHeader(value = "X-Operator-Id", required = false) Long operatorId,
                            @RequestHeader(value = "X-Operator-Role", required = false) OperatorRole operatorRole) {
        return ApiResponse.ok(fineAppService.listFines(readerId, Operator.of(operatorId, operatorRole)));
    }

    @PostMapping("/{fineId}/pay")
    public ApiResponse pay(@PathVariable Long fineId,
                           @RequestHeader(value = "X-Operator-Id", required = false) Long operatorId,
                           @RequestHeader(value = "X-Operator-Role", required = false) OperatorRole operatorRole) {
        return ApiResponse.ok("罚款已缴纳", fineAppService.pay(fineId, Operator.of(operatorId, operatorRole)));
    }
}
