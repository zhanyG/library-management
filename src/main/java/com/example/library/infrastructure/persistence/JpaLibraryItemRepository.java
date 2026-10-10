package com.example.library.infrastructure.persistence;

import com.example.library.domain.model.catalog.ItemStatus;
import com.example.library.domain.model.catalog.LibraryItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * LibraryItem 的 Spring Data JPA 接口（派生查询）。
 */
public interface JpaLibraryItemRepository extends JpaRepository<LibraryItem, Long> {

    Optional<LibraryItem> findByBarcode(String barcode);
    boolean existsByBarcode(String barcode);
    List<LibraryItem> findByBookTitleId(Long titleId);
    boolean existsByBookTitleIdAndStatus(Long titleId, ItemStatus status);
    long countByBookTitleIdAndStatus(Long titleId, ItemStatus status);
    long countByBookTitleId(Long titleId);
}
