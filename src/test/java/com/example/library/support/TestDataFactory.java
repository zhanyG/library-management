package com.example.library.support;

import com.example.library.domain.model.catalog.BookTitle;
import com.example.library.domain.model.catalog.ItemType;
import com.example.library.domain.model.catalog.LibraryItem;
import com.example.library.domain.model.catalog.LibraryItemFactory;
import com.example.library.domain.model.circulation.Loan;
import com.example.library.domain.model.reader.BorrowCard;
import com.example.library.domain.model.reader.Reader;
import com.example.library.domain.model.reader.ReaderType;
import com.example.library.domain.model.reader.StudentReader;
import com.example.library.domain.repository.BookTitleRepository;
import com.example.library.domain.repository.BorrowCardRepository;
import com.example.library.domain.repository.LibraryItemRepository;
import com.example.library.domain.repository.LoanRepository;
import com.example.library.domain.repository.ReaderRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 测试数据构造器：为集成测试提供读者、借阅证、题名、副本与借阅记录。
 */
@Component
public class TestDataFactory {

    private static final AtomicInteger SEQUENCE = new AtomicInteger(0);

    private final ReaderRepository readerRepository;
    private final BorrowCardRepository borrowCardRepository;
    private final BookTitleRepository bookTitleRepository;
    private final LibraryItemRepository libraryItemRepository;
    private final LoanRepository loanRepository;

    public TestDataFactory(ReaderRepository readerRepository,
                           BorrowCardRepository borrowCardRepository,
                           BookTitleRepository bookTitleRepository,
                           LibraryItemRepository libraryItemRepository,
                           LoanRepository loanRepository) {
        this.readerRepository = readerRepository;
        this.borrowCardRepository = borrowCardRepository;
        this.bookTitleRepository = bookTitleRepository;
        this.libraryItemRepository = libraryItemRepository;
        this.loanRepository = loanRepository;
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

    public BookTitle createTitle(ItemType itemType) {
        String isbn = "978-7-111-" + String.format("%05d", SEQUENCE.incrementAndGet()) + "-0";
        return bookTitleRepository.save(new BookTitle("测试题名" + SEQUENCE.get(), "测试作者", isbn,
                "测试出版社", 2024, itemType));
    }

    public LibraryItem addItem(BookTitle title, ItemType itemType) {
        LibraryItem item = LibraryItemFactory.create(itemType, nextBarcode(), "A区1排");
        title.addItem(item);
        bookTitleRepository.save(title);
        return item;
    }

    public Loan createLoan(BorrowCard card, LibraryItem item, LocalDate borrowDate, LocalDate dueDate) {
        return loanRepository.save(new Loan(card.getId(), card.getReaderId(), item.getId(),
                1L, borrowDate, dueDate));
    }

    public String nextBarcode() {
        return "ITEM2026" + String.format("%06d", SEQUENCE.incrementAndGet());
    }

    public LibraryItemRepository itemRepository() {
        return libraryItemRepository;
    }
}
