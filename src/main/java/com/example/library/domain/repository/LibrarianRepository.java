package com.example.library.domain.repository;
import com.example.library.domain.model.reader.Librarian;
import java.util.Optional;

/**
 * 图书管理员持久化端口。
 *
 * <p>领域层端口：只声明业务语义方法，不依赖任何框架。</p>
 */
public interface LibrarianRepository {

    Librarian save(Librarian librarian);
    Optional<Librarian> findById(Long id);
    Optional<Librarian> findByStaffNo(String staffNo);
    boolean existsByStaffNo(String staffNo);
    void delete(Librarian librarian);
}
