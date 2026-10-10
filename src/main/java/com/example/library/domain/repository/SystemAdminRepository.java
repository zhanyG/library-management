package com.example.library.domain.repository;
import com.example.library.domain.model.reader.SystemAdmin;
import java.util.Optional;

/**
 * 系统管理员持久化端口。
 *
 * <p>领域层端口：只声明业务语义方法，不依赖任何框架。</p>
 */
public interface SystemAdminRepository {

    SystemAdmin save(SystemAdmin admin);
    Optional<SystemAdmin> findById(Long id);
    boolean existsById(Long id);
}
