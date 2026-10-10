package com.example.library.presentation.controller;

import com.example.library.application.AdminAppService;
import com.example.library.application.Operator;
import com.example.library.application.OperatorRole;
import com.example.library.domain.model.catalog.ItemType;
import com.example.library.domain.model.reader.ReaderType;
import com.example.library.presentation.dto.AddLibrarianRequest;
import com.example.library.presentation.dto.ApiResponse;
import com.example.library.presentation.dto.UpdateBorrowPolicyRequest;
import com.example.library.presentation.dto.UpdateFineRuleRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统管理员接口：管理员账号与规则维护（仅系统管理员）。
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminAppService adminAppService;

    public AdminController(AdminAppService adminAppService) {
        this.adminAppService = adminAppService;
    }

    @PostMapping("/librarians")
    public ResponseEntity<ApiResponse> addLibrarian(@Valid @RequestBody AddLibrarianRequest request,
                                                    @RequestHeader(value = "X-Operator-Id", required = false) Long operatorId,
                                                    @RequestHeader(value = "X-Operator-Role", required = false) OperatorRole operatorRole) {
        adminAppService.addLibrarian(request, Operator.of(operatorId, operatorRole));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("图书管理员已添加", null));
    }

    @DeleteMapping("/librarians/{staffNo}")
    public ApiResponse removeLibrarian(@PathVariable String staffNo,
                                       @RequestHeader(value = "X-Operator-Id", required = false) Long operatorId,
                                       @RequestHeader(value = "X-Operator-Role", required = false) OperatorRole operatorRole) {
        adminAppService.removeLibrarian(staffNo, Operator.of(operatorId, operatorRole));
        return ApiResponse.ok("图书管理员已停用", null);
    }

    @PutMapping("/borrow-policies/{readerType}")
    public ApiResponse updateBorrowPolicy(@PathVariable ReaderType readerType,
                                          @Valid @RequestBody UpdateBorrowPolicyRequest request,
                                          @RequestHeader(value = "X-Operator-Id", required = false) Long operatorId,
                                          @RequestHeader(value = "X-Operator-Role", required = false) OperatorRole operatorRole) {
        adminAppService.updateBorrowPolicy(readerType, request, Operator.of(operatorId, operatorRole));
        return ApiResponse.ok("借阅规则已更新", null);
    }

    @PutMapping("/fine-rules/{itemType}")
    public ApiResponse updateFineRule(@PathVariable ItemType itemType,
                                      @Valid @RequestBody UpdateFineRuleRequest request,
                                      @RequestHeader(value = "X-Operator-Id", required = false) Long operatorId,
                                      @RequestHeader(value = "X-Operator-Role", required = false) OperatorRole operatorRole) {
        adminAppService.updateFineRule(itemType, request, Operator.of(operatorId, operatorRole));
        return ApiResponse.ok("罚款规则已更新", null);
    }
}
