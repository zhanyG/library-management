package com.example.library.infrastructure.persistence;

import com.example.library.domain.model.reader.SystemAdmin;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * SystemAdmin 的 Spring Data JPA 接口（派生查询）。
 */
public interface JpaSystemAdminRepository extends JpaRepository<SystemAdmin, Long> {
}
