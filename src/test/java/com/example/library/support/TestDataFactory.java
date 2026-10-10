package com.example.library.support;

import com.example.library.domain.model.catalog.BookTitle;
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
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 测试数据构造器：为集成测试提供读者、借阅证、题名、副本、借阅记录与罚款记录。
 */
@Component
public class TestDataFactory {

    private static final AtomicInteger SEQUENCE = new AtomicInteger(0);

    private final ReaderRepository readerRepository;
    private final BorrowCardRepository borrowCardRepository;
    private final BookTitleRepository bookTitleRepository;
    private final LibraryItemRepository libraryItemRepository;
    private final LoanRepository loanRepository;
    private final FineRecordRepository fineRecordRepository;

    public TestDataFactory(ReaderRepository readerRepository,
                           BorrowCardRepository borrowCardRepository,
                           BookTitleRepository bookTitleRepository,
                           LibraryItemRepository libraryItemRepository,
                           LoanRepository loanRepository,
                           FineRecordRepository fineRecordRepository) {
        this.readerRepository = readerRepository;
        this.borrowCardRepository = borrowCardRepository;
        this.bookTitleRepository = bookTitleRepository;
        this.libraryItemRepository = libraryItemRepository;
        this.loanRepository = loanRepository;
        this.fineRecordRepository = fineRecordRepository;
    }

    public Reader createReader(String name, String idCardNo, ReaderType readerType) {
        return readerRepository.save(new StudentReader(name, idCardNo, "13800000000", "计算机学院",
                readerType, "S" + idCardNo, "2024", "软件工程"));
    }

    public BorrowCard createCard(Reader reader) {
        String cardNo = "CARD2026" + String.format("%06d", SEQUENCE.incrementAndGet());
        return borrowCardRepository.save(new BorrowCard(cardNo, reader.getId(), reader.getName(),
                reader.getDepartment(), LocalDate.now()));
    }

    public BorrowCard cardOf(Reader reader) {
        return borrowCardRepository.findByReaderId(reader.getId()).orElseGet(() -> createCard(reader));
    }

    public BookTitle createTitle(ItemType itemType) {
        String isbn = "978-7-111-" + String.format("%05d", SEQUENCE.incrementAndGet()) + "-0";
        return bookTitleRepository.save(new BookTitle("测试题名" + SEQUENCE.get(), "测试作者", isbn,
                "测试出版社", 2024, itemType));
    }

    /** 新增可借副本。 */
    public LibraryItem addItem(BookTitle title, ItemType itemType) {
        LibraryItem item = LibraryItemFactory.create(itemType, nextBarcode(), "A区1排");
        item.attachTo(title);
        return libraryItemRepository.save(item);
    }

    /** 新增处于借出状态的副本。 */
    public LibraryItem addOnLoanItem(BookTitle title, ItemType itemType) {
        LibraryItem item = addItem(title, itemType);
        item.markOnLoan();
        return libraryItemRepository.save(item);
    }

    public Loan createLoan(BorrowCard card, LibraryItem item, LocalDate borrowDate, LocalDate dueDate) {
        return loanRepository.save(new Loan(card.getId(), card.getReaderId(), item.getId(), 1L, borrowDate, dueDate));
    }

    /** 为读者构造一条在借（未超期）记录，副本同步置为借出。 */
    public Loan createActiveLoan(Reader reader, LocalDate borrowDate, LocalDate dueDate) {
        BorrowCard card = cardOf(reader);
        BookTitle title = createTitle(ItemType.CHINESE_BOOK);
        LibraryItem item = addOnLoanItem(title, ItemType.CHINESE_BOOK);
        return createLoan(card, item, borrowDate, dueDate);
    }

    /** 为读者构造未缴罚款。 */
    public FineRecord createUnpaidFine(Reader reader) {
        long syntheticLoanId = (long) SEQUENCE.incrementAndGet() * 1000;
        return fineRecordRepository.save(new FineRecord(syntheticLoanId, reader.getId(), Money.of("5.00")));
    }

    public String nextBarcode() {
        return "ITEM2026" + String.format("%06d", SEQUENCE.incrementAndGet());
    }
}
