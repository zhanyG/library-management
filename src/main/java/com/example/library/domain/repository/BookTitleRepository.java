package com.example.library.domain.repository;
import com.example.library.domain.model.catalog.BookTitle;
import java.util.List;
import java.util.Optional;

/**
 * 图书标题持久化端口。
 *
 * <p>领域层端口：只声明业务语义方法，不依赖任何框架。</p>
 */
public interface BookTitleRepository {

    BookTitle save(BookTitle title);
    Optional<BookTitle> findById(Long id);
    boolean existsByIsbn(String isbn);
    List<BookTitle> searchByKeyword(String keyword);
    void delete(BookTitle title);
}
