package com.example.library.infrastructure.persistence;

import com.example.library.domain.model.catalog.ItemStatus;
import com.example.library.domain.model.catalog.LibraryItem;
import com.example.library.domain.repository.LibraryItemRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * LibraryItemRepository 的持久化适配器：把领域端口委托给 Spring Data JPA。
 */
@Component
public class LibraryItemRepositoryAdapter implements LibraryItemRepository {

    private final JpaLibraryItemRepository jpa;

    public LibraryItemRepositoryAdapter(JpaLibraryItemRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public LibraryItem save(LibraryItem item) {
        return jpa.save(item);
    }

    @Override
    public Optional<LibraryItem> findById(Long id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<LibraryItem> findByBarcode(String barcode) {
        return jpa.findByBarcode(barcode);
    }

    @Override
    public boolean existsByBarcode(String barcode) {
        return jpa.existsByBarcode(barcode);
    }

    @Override
    public List<LibraryItem> findByTitleId(Long titleId) {
        return jpa.findByBookTitleId(titleId);
    }

    @Override
    public boolean existsOnLoanByTitleId(Long titleId) {
        return jpa.existsByBookTitleIdAndStatus(titleId, ItemStatus.ON_LOAN);
    }

    @Override
    public long countAvailableByTitleId(Long titleId) {
        return jpa.countByBookTitleIdAndStatus(titleId, ItemStatus.AVAILABLE);
    }

    @Override
    public long countByTitleId(Long titleId) {
        return jpa.countByBookTitleId(titleId);
    }

    @Override
    public long count() {
        return jpa.count();
    }

    @Override
    public void delete(LibraryItem item) {
        jpa.delete(item);
    }
}
