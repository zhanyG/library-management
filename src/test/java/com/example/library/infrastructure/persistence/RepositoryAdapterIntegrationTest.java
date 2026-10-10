package com.example.library.infrastructure.persistence;

import com.example.library.domain.model.catalog.BookTitle;
import com.example.library.domain.model.catalog.ItemStatus;
import com.example.library.domain.model.catalog.ItemType;
import com.example.library.domain.model.catalog.LibraryItem;
import com.example.library.domain.model.catalog.LibraryItemFactory;
import com.example.library.domain.model.circulation.Loan;
import com.example.library.domain.model.fine.FineRecord;
import com.example.library.domain.model.fine.Money;
import com.example.library.domain.model.reader.BorrowCard;
import com.example.library.domain.model.reader.Reader;
import com.example.library.domain.model.reader.ReaderType;
import com.example.library.domain.model.reader.StudentReader;
import com.example.library.domain.repository.BookTitleRepository;
import com.example.library.domain.repository.BorrowCardRepository;
import com.example.library.domain.repository.FineRecordRepository;
import com.example.library.domain.repository.LibraryItemRepository;
import com.example.library.domain.repository.LoanRepository;
import com.example.library.domain.repository.ReaderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TASK-003 持久化适配器集成测试：验证领域端口方法在 H2 上按业务语义工作。
 */
@SpringBootTest
@Transactional
class RepositoryAdapterIntegrationTest {

    @Autowired
    private ReaderRepository readerRepository;

    @Autowired
    private BorrowCardRepository borrowCardRepository;

    @Autowired
    private BookTitleRepository bookTitleRepository;

    @Autowired
    private LibraryItemRepository libraryItemRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private FineRecordRepository fineRecordRepository;

    private Reader saveReader() {
        return readerRepository.save(new StudentReader(
                "张三", "20240001", "13800000000", "计算机学院",
                ReaderType.UNDERGRADUATE, "S2024001", "2024", "软件工程"));
    }

    @Test
    @DisplayName("读者按证件号查询与唯一性判定")
    void readerPersistence() {
        Reader saved = saveReader();

        assertThat(saved.getId()).isNotNull();
        assertThat(readerRepository.findByIdCardNo("20240001")).isPresent();
        assertThat(readerRepository.existsByIdCardNo("20240001")).isTrue();
        assertThat(readerRepository.existsByIdCardNo("99999999")).isFalse();
    }

    @Test
    @DisplayName("借阅证按证号查询并支持有效证判定")
    void borrowCardPersistence() {
        Reader reader = saveReader();
        BorrowCard card = borrowCardRepository.save(
                new BorrowCard("CARD2026000001", reader.getId(), "张三", "计算机学院", LocalDate.now()));

        assertThat(card.getId()).isNotNull();
        assertThat(borrowCardRepository.findByCardNo("CARD2026000001")).isPresent();
        assertThat(borrowCardRepository.findByReaderId(reader.getId())).isPresent();
        assertThat(borrowCardRepository.existsValidCardByReaderId(reader.getId())).isTrue();
        assertThat(borrowCardRepository.count()).isPositive();
    }

    @Test
    @DisplayName("题名与副本保存、可借副本统计、题名关键字检索")
    void catalogPersistence() {
        BookTitle title = new BookTitle("软件体系结构", "某人", "978-7-111-00000-0",
                "机械工业出版社", 2024, ItemType.CHINESE_BOOK);
        LibraryItem item = LibraryItemFactory.create(ItemType.CHINESE_BOOK, "ITEM2026000001", "A区3排2层");
        title.addItem(item);
        LibraryItem second = LibraryItemFactory.create(ItemType.CHINESE_BOOK, "ITEM2026000002", "A区3排2层");
        title.addItem(second);

        BookTitle saved = bookTitleRepository.save(title);
        second.markOnLoan();
        libraryItemRepository.save(second);

        assertThat(saved.getId()).isNotNull();
        assertThat(libraryItemRepository.findByBarcode("ITEM2026000001")).isPresent();
        assertThat(libraryItemRepository.countByTitleId(saved.getId())).isEqualTo(2);
        assertThat(libraryItemRepository.countAvailableByTitleId(saved.getId())).isEqualTo(1);
        assertThat(libraryItemRepository.findByTitleId(saved.getId())).hasSize(2);
        assertThat(bookTitleRepository.searchByKeyword("软件")).hasSize(1);
        assertThat(bookTitleRepository.existsByIsbn("978-7-111-00000-0")).isTrue();
    }

    @Test
    @DisplayName("在借数量统计、超期判定与按副本查未还借阅")
    void loanPersistence() {
        Reader reader = saveReader();
        BorrowCard card = borrowCardRepository.save(
                new BorrowCard("CARD2026000001", reader.getId(), "张三", "计算机学院", LocalDate.now()));
        BookTitle title = new BookTitle("软件体系结构", "某人", "978-7-111-00000-0",
                "机械工业出版社", 2024, ItemType.CHINESE_BOOK);
        LibraryItem item = LibraryItemFactory.create(ItemType.CHINESE_BOOK, "ITEM2026000001", "A区");
        title.addItem(item);
        bookTitleRepository.save(title);

        LocalDate today = LocalDate.of(2026, 11, 20);
        Loan loan = loanRepository.save(new Loan(card.getId(), reader.getId(), item.getId(), 1L,
                today.minusDays(40), today.minusDays(10)));

        assertThat(loan.getId()).isNotNull();
        assertThat(loanRepository.countActiveByReaderId(reader.getId())).isEqualTo(1);
        assertThat(loanRepository.existsOverdueByReaderId(reader.getId(), today)).isTrue();
        assertThat(loanRepository.existsOverdueByReaderId(reader.getId(), today.minusDays(20))).isFalse();
        assertThat(loanRepository.findActiveByItemId(item.getId())).isPresent();

        loan.markReturned(today);
        loanRepository.save(loan);

        assertThat(loanRepository.countActiveByReaderId(reader.getId())).isZero();
        assertThat(loanRepository.findActiveByItemId(item.getId())).isEmpty();
        assertThat(loanRepository.findByReaderId(reader.getId())).hasSize(1);
    }

    @Test
    @DisplayName("未缴罚款判定与罚款金额持久化")
    void fineRecordPersistence() {
        Reader reader = saveReader();
        fineRecordRepository.save(new FineRecord(1L, reader.getId(), Money.of("3.40")));

        assertThat(fineRecordRepository.existsUnpaidByReaderId(reader.getId())).isTrue();
        assertThat(fineRecordRepository.findByReaderId(reader.getId()))
                .singleElement()
                .satisfies(record -> assertThat(record.getAmount().amount()).isEqualByComparingTo("3.40"));
    }

    @Test
    @DisplayName("副本状态机：借出与归还")
    void itemStatusMachine() {
        BookTitle title = new BookTitle("软件体系结构", "某人", "978-7-111-00000-1",
                "机械工业出版社", 2024, ItemType.CHINESE_BOOK);
        LibraryItem item = LibraryItemFactory.create(ItemType.CHINESE_BOOK, "ITEM2026000010", "A区");
        title.addItem(item);
        bookTitleRepository.save(title);

        assertThat(item.isAvailable()).isTrue();

        item.markOnLoan();
        libraryItemRepository.save(item);
        LibraryItem onLoan = libraryItemRepository.findByBarcode("ITEM2026000010").orElseThrow();

        assertThat(onLoan.getStatus()).isEqualTo(ItemStatus.ON_LOAN);
        assertThat(onLoan.isAvailable()).isFalse();
        assertThat(libraryItemRepository.existsOnLoanByTitleId(title.getId())).isTrue();
    }
}
