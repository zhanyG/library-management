package com.example.library.infrastructure.persistence;

import com.example.library.domain.model.catalog.ItemType;
import com.example.library.domain.model.fine.FineRule;
import com.example.library.domain.repository.FineRuleRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * FineRuleRepository 的持久化适配器：把领域端口委托给 Spring Data JPA。
 */
@Component
public class FineRuleRepositoryAdapter implements FineRuleRepository {

    private final JpaFineRuleRepository jpa;

    public FineRuleRepositoryAdapter(JpaFineRuleRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public FineRule save(FineRule rule) {
        return jpa.save(rule);
    }

    @Override
    public Optional<FineRule> findByItemType(ItemType itemType) {
        return jpa.findByItemType(itemType);
    }

    @Override
    public List<FineRule> findAll() {
        return jpa.findAll();
    }
}
