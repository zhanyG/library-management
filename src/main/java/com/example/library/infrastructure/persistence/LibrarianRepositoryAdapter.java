package com.example.library.infrastructure.persistence;

import com.example.library.domain.model.reader.Librarian;
import com.example.library.domain.repository.LibrarianRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * LibrarianRepository 的持久化适配器：把领域端口委托给 Spring Data JPA。
 */
@Component
public class LibrarianRepositoryAdapter implements LibrarianRepository {

    private final JpaLibrarianRepository jpa;

    public LibrarianRepositoryAdapter(JpaLibrarianRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Librarian save(Librarian librarian) {
        return jpa.save(librarian);
    }

    @Override
    public Optional<Librarian> findById(Long id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<Librarian> findByStaffNo(String staffNo) {
        return jpa.findByStaffNo(staffNo);
    }

    @Override
    public boolean existsByStaffNo(String staffNo) {
        return jpa.existsByStaffNo(staffNo);
    }

    @Override
    public void delete(Librarian librarian) {
        jpa.delete(librarian);
    }
}
