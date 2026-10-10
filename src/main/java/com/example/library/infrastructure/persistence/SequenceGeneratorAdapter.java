package com.example.library.infrastructure.persistence;

import com.example.library.domain.repository.BorrowCardRepository;
import com.example.library.domain.repository.LibraryItemRepository;
import com.example.library.domain.service.NumberGenerator;
import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 编号生成适配器：借阅证号 CARD+年份+6位序号、馆藏条码 ITEM+年份+6位序号。
 *
 * <p>通过“递增 + 存在性校验”保证唯一性（BR-001 与条码唯一约束）。</p>
 */
@Component
public class SequenceGeneratorAdapter implements NumberGenerator {

    private static final String CARD_PREFIX = "CARD";
    private static final String ITEM_PREFIX = "ITEM";

    private final BorrowCardRepository borrowCardRepository;
    private final LibraryItemRepository libraryItemRepository;
    private final AtomicLong cardSequence = new AtomicLong();
    private final AtomicLong itemSequence = new AtomicLong();

    public SequenceGeneratorAdapter(BorrowCardRepository borrowCardRepository,
                                    LibraryItemRepository libraryItemRepository) {
        this.borrowCardRepository = borrowCardRepository;
        this.libraryItemRepository = libraryItemRepository;
        this.cardSequence.set(borrowCardRepository.count());
        this.itemSequence.set(libraryItemRepository.count());
    }

    @Override
    public String nextCardNo() {
        String year = String.valueOf(Year.now().getValue());
        String candidate;
        do {
            candidate = CARD_PREFIX + year + String.format("%06d", cardSequence.incrementAndGet());
        } while (borrowCardRepository.existsByCardNo(candidate));
        return candidate;
    }

    @Override
    public String nextBarcode() {
        String year = String.valueOf(Year.now().getValue());
        String candidate;
        do {
            candidate = ITEM_PREFIX + year + String.format("%06d", itemSequence.incrementAndGet());
        } while (libraryItemRepository.existsByBarcode(candidate));
        return candidate;
    }
}
