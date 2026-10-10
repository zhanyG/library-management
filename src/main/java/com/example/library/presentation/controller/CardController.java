package com.example.library.presentation.controller;

import com.example.library.application.CardAppService;
import com.example.library.application.Operator;
import com.example.library.application.OperatorRole;
import com.example.library.presentation.dto.ApiResponse;
import com.example.library.presentation.dto.CardResponse;
import com.example.library.presentation.dto.IssueCardRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 借阅证管理接口（仅系统管理员，BR-014）。
 */
@RestController
@RequestMapping("/api/admin/borrow-cards")
public class CardController {

    private final CardAppService cardAppService;

    public CardController(CardAppService cardAppService) {
        this.cardAppService = cardAppService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse> issue(@Valid @RequestBody IssueCardRequest request,
                                             @RequestHeader(value = "X-Operator-Id", required = false) Long operatorId,
                                             @RequestHeader(value = "X-Operator-Role", required = false) OperatorRole operatorRole) {
        CardResponse response = cardAppService.issueCard(request.readerId(), Operator.of(operatorId, operatorRole));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("借阅证办理成功", response));
    }

    @DeleteMapping("/{cardNo}")
    public ApiResponse revoke(@PathVariable String cardNo,
                              @RequestHeader(value = "X-Operator-Id", required = false) Long operatorId,
                              @RequestHeader(value = "X-Operator-Role", required = false) OperatorRole operatorRole) {
        cardAppService.revokeCard(cardNo, Operator.of(operatorId, operatorRole));
        return ApiResponse.ok("借阅证已注销", null);
    }
}
