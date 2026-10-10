package com.example.library.presentation.controller;

import com.example.library.application.Operator;
import com.example.library.application.OperatorRole;
import com.example.library.application.ReservationAppService;
import com.example.library.presentation.dto.ApiResponse;
import com.example.library.presentation.dto.ReserveBookRequest;
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
 * 预约接口（UC-004、UC-005）。
 */
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationAppService reservationAppService;

    public ReservationController(ReservationAppService reservationAppService) {
        this.reservationAppService = reservationAppService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse> reserve(@Valid @RequestBody ReserveBookRequest request,
                                               @RequestHeader(value = "X-Operator-Id", required = false) Long operatorId,
                                               @RequestHeader(value = "X-Operator-Role", required = false) OperatorRole operatorRole) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("预约成功",
                reservationAppService.reserve(request, Operator.of(operatorId, operatorRole))));
    }

    @DeleteMapping("/{reservationId}")
    public ApiResponse cancel(@PathVariable Long reservationId,
                              @RequestHeader(value = "X-Operator-Id", required = false) Long operatorId,
                              @RequestHeader(value = "X-Operator-Role", required = false) OperatorRole operatorRole) {
        reservationAppService.cancel(reservationId, Operator.of(operatorId, operatorRole));
        return ApiResponse.ok("预约已取消", null);
    }
}
