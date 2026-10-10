package com.example.library.infrastructure.persistence;

import com.example.library.domain.model.reader.Librarian;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Librarian 的 Spring Data JPA 接口（派生查询）。
 */
public interface JpaLibrarianRepository extends JpaRepository<Librarian, Long> {

    Optional<Librarian> findByStaffNo(String staffNo);
    boolean existsByStaffNo(String staffNo);
}
