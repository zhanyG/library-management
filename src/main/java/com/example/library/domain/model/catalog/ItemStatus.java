package com.example.library.domain.model.catalog;

/**
 * 馆藏副本状态（BR-006：仅 AVAILABLE 可借）。
 */
public enum ItemStatus {
    AVAILABLE,
    ON_LOAN,
    RESERVED,
    WITHDRAWN,
    LOST,
    UNDER_REPAIR
}
