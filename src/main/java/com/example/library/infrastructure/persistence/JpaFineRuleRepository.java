package com.example.library.infrastructure.persistence;

import com.example.library.domain.model.catalog.ItemType;
import com.example.library.domain.model.fine.FineRule;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * FineRule 的 Spring Data JPA 接口（派生查询）。
 */
public interface JpaFineRuleRepository extends JpaRepository<FineRule, Long> {

    Optional<FineRule> findByItemType(ItemType itemType);
}
