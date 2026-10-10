package com.example.library.presentation.controller;

import com.example.library.application.CatalogAppService;
import com.example.library.application.Operator;
import com.example.library.application.OperatorRole;
import com.example.library.presentation.dto.AddItemRequest;
import com.example.library.presentation.dto.ApiResponse;
import com.example.library.presentation.dto.CreateTitleRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 目录接口：图书查询与题名/馆藏维护（维护仅系统管理员）。
 */
@RestController
@RequestMapping("/api")
public class CatalogController {

    private final CatalogAppService catalogAppService;

    public CatalogController(CatalogAppService catalogAppService) {
        this.catalogAppService = catalogAppService;
    }

    @GetMapping("/catalog/titles")
    public ApiResponse search(@RequestParam(value = "keyword", required = false) String keyword) {
        return ApiResponse.ok(catalogAppService.searchTitles(keyword));
    }

    @PostMapping("/admin/book-titles")
    public ResponseEntity<ApiResponse> createTitle(@Valid @RequestBody CreateTitleRequest request,
                                                  @RequestHeader(value = "X-Operator-Id", required = false) Long operatorId,
                                                  @RequestHeader(value = "X-Operator-Role", required = false) OperatorRole operatorRole) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("图书标题添加成功",
                catalogAppService.createTitle(request, Operator.of(operatorId, operatorRole))));
    }

    @DeleteMapping("/admin/book-titles/{titleId}")
    public ApiResponse deleteTitle(@PathVariable Long titleId,
                                   @RequestHeader(value = "X-Operator-Id", required = false) Long operatorId,
                                   @RequestHeader(value = "X-Operator-Role", required = false) OperatorRole operatorRole) {
        catalogAppService.deleteTitle(titleId, Operator.of(operatorId, operatorRole));
        return ApiResponse.ok("图书标题已删除", null);
    }

    @PostMapping("/admin/library-items")
    public ResponseEntity<ApiResponse> addItem(@Valid @RequestBody AddItemRequest request,
                                              @RequestHeader(value = "X-Operator-Id", required = false) Long operatorId,
                                              @RequestHeader(value = "X-Operator-Role", required = false) OperatorRole operatorRole) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("馆藏副本添加成功",
                catalogAppService.addItem(request, Operator.of(operatorId, operatorRole))));
    }

    @DeleteMapping("/admin/library-items/{barcode}")
    public ApiResponse removeItem(@PathVariable String barcode,
                                  @RequestHeader(value = "X-Operator-Id", required = false) Long operatorId,
                                  @RequestHeader(value = "X-Operator-Role", required = false) OperatorRole operatorRole) {
        catalogAppService.removeItem(barcode, Operator.of(operatorId, operatorRole));
        return ApiResponse.ok("馆藏副本已下架", null);
    }
}
