package com.example.library.domain.repository;
import com.example.library.domain.model.catalog.LibraryItem;
import java.util.List;
import java.util.Optional;

/**
 * 馆藏副本持久化端口。
 *
 * <p>领域层端口：只声明业务语义方法，不依赖任何框架。</p>
 */
public interface LibraryItemRepository {

    LibraryItem save(LibraryItem item);
    Optional<LibraryItem> findById(Long id);
    Optional<LibraryItem> findByBarcode(String barcode);
    boolean existsByBarcode(String barcode);
    List<LibraryItem> findByTitleId(Long titleId);
    boolean existsOnLoanByTitleId(Long titleId);
    long countAvailableByTitleId(Long titleId);
    long countByTitleId(Long titleId);
    long count();
    void delete(LibraryItem item);
}
