package com.example.library.infrastructure.persistence;

import com.example.library.domain.model.catalog.BookTitle;
import com.example.library.domain.repository.BookTitleRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * BookTitleRepository 的持久化适配器：把领域端口委托给 Spring Data JPA。
 */
@Component
public class BookTitleRepositoryAdapter implements BookTitleRepository {

    private final JpaBookTitleRepository jpa;

    public BookTitleRepositoryAdapter(JpaBookTitleRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public BookTitle save(BookTitle title) {
        return jpa.save(title);
    }

    @Override
    public Optional<BookTitle> findById(Long id) {
        return jpa.findById(id);
    }

    @Override
    public boolean existsByIsbn(String isbn) {
        return jpa.existsByIsbn(isbn);
    }

    @Override
    public List<BookTitle> searchByKeyword(String keyword) {
        return jpa.findByNameContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrIsbnContainingIgnoreCase(keyword, keyword, keyword);
    }

    @Override
    public void delete(BookTitle title) {
        jpa.delete(title);
    }
}
