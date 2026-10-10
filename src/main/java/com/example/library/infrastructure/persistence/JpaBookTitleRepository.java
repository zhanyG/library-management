package com.example.library.infrastructure.persistence;

import com.example.library.domain.model.catalog.BookTitle;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * BookTitle 的 Spring Data JPA 接口（派生查询）。
 */
public interface JpaBookTitleRepository extends JpaRepository<BookTitle, Long> {

    boolean existsByIsbn(String isbn);
    List<BookTitle> findByNameContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrIsbnContainingIgnoreCase(String name, String author, String isbn);
}
