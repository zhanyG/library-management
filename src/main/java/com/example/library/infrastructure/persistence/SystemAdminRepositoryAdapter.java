package com.example.library.infrastructure.persistence;

import com.example.library.domain.model.reader.SystemAdmin;
import com.example.library.domain.repository.SystemAdminRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * SystemAdminRepository 的持久化适配器：把领域端口委托给 Spring Data JPA。
 */
@Component
public class SystemAdminRepositoryAdapter implements SystemAdminRepository {

    private final JpaSystemAdminRepository jpa;

    public SystemAdminRepositoryAdapter(JpaSystemAdminRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public SystemAdmin save(SystemAdmin admin) {
        return jpa.save(admin);
    }

    @Override
    public Optional<SystemAdmin> findById(Long id) {
        return jpa.findById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpa.existsById(id);
    }
}
