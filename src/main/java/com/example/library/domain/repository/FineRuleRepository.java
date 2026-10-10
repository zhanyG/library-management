package com.example.library.domain.repository;
import com.example.library.domain.model.catalog.ItemType;
import com.example.library.domain.model.fine.FineRule;
import java.util.List;
import java.util.Optional;

/**
 * 罚款规则持久化端口。
 *
 * <p>领域层端口：只声明业务语义方法，不依赖任何框架。</p>
 */
public interface FineRuleRepository {

    FineRule save(FineRule rule);
    Optional<FineRule> findByItemType(ItemType itemType);
    List<FineRule> findAll();
}
