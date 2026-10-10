package com.example.library.presentation.dto;

/**
 * 馆藏副本响应。
 */
public record ItemResponse(String barcode, String titleName, String itemType, String status, String location) {
}
