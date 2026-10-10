package com.example.library.application;

import com.example.library.domain.model.catalog.ItemStatus;
import com.example.library.domain.model.catalog.ItemType;
import com.example.library.domain.model.catalog.LibraryItem;
import com.example.library.domain.model.reader.BorrowCard;
import com.example.library.domain.model.reader.Reader;
import com.example.library.domain.model.reader.ReaderType;
import com.example.library.domain.model.reservation.Reservation;
import com.example.library.domain.repository.BorrowCardRepository;
import com.example.library.domain.repository.LibraryItemRepository;
import com.example.library.domain.repository.LoanRepository;
import com.example.library.domain.repository.ReservationRepository;
import com.example.library.exception.CardInvalidException;
import com.example.library.exception.ItemNotAvailableException;
import com.example.library.exception.ItemNotLoanableException;
import com.example.library.exception.ItemReservedByOtherException;
import com.example.library.exception.OverdueLoanExistsException;
import com.example.library.exception.PermissionDeniedException;
import com.example.library.exception.QuotaExceededException;
import com.example.library.exception.UnpaidFineExistsException;
import com.example.library.presentation.dto.BorrowBookRequest;
import com.example.library.presentation.dto.LoanResponse;
import com.example.library.support.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TASK-009 办理借书集成测试（TC-012 ~ TC-021）。
 */
@SpringBootTest
@Transactional
class BorrowBookAppServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 10);

    @Autowired
    private CirculationAppService circulationAppService;

    @Autowired
    private BorrowCardRepository borrowCardRepository;

    @Autowired
    private LibraryItemRepository libraryItemRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private TestDataFactory dataFactory;

    private final Operator librarian = Operator.librarian(7L);

    private Reader reader;

    private BorrowCard card;

    private LibraryItem item;

    private void givenBorrowableItem() {
        reader = dataFactory.createReader("张三", "20240001", ReaderType.UNDERGRADUATE);
        card = dataFactory.createCard(reader);
        item = dataFactory.addItem(dataFactory.createTitle(ItemType.CHINESE_BOOK), ItemType.CHINESE_BOOK);
    }

    private LoanResponse borrow() {
        return circulationAppService.borrowBook(
                new BorrowBookRequest(card.getCardNo(), item.getBarcode()), librarian);
    }

    @Test
    @DisplayName("TC-012 借书成功：应还日期 = 借出日期 + 读者类型期限，副本置为 ON_LOAN")
    void borrowSuccess() {
        givenBorrowableItem();

        LoanResponse response = borrow();

        assertThat(response.loanId()).isNotNull();
        assertThat(response.readerName()).isEqualTo("张三");
        assertThat(response.borrowDate()).isEqualTo(LocalDate.now());
        assertThat(response.dueDate()).isEqualTo(LocalDate.now().plusDays(30));
        assertThat(response.status()).isEqualTo("BORROWED");

        LibraryItem after = libraryItemRepository.findByBarcode(item.getBarcode()).orElseThrow();
        assertThat(after.getStatus()).isEqualTo(ItemStatus.ON_LOAN);
        assertThat(loanRepository.countActiveByReaderId(reader.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("TC-013 借阅证无效时借书失败（BR-002）")
    void borrowWithRevokedCard() {
        givenBorrowableItem();
        card.revoke();
        borrowCardRepository.save(card);

        assertThatThrownBy(this::borrow).isInstanceOf(CardInvalidException.class);
        assertThat(loanRepository.countActiveByReaderId(reader.getId())).isZero();
    }

    @Test
    @DisplayName("TC-014 超过借阅数量时借书失败（BR-003）")
    void borrowExceedsQuota() {
        givenBorrowableItem();
        for (int i = 0; i < 5; i++) {
            dataFactory.createActiveLoan(reader, TODAY.minusDays(1), TODAY.plusDays(29));
        }

        assertThatThrownBy(this::borrow)
                .isInstanceOf(QuotaExceededException.class)
                .hasMessageContaining("最多可借 5 本");

        LibraryItem after = libraryItemRepository.findByBarcode(item.getBarcode()).orElseThrow();
        assertThat(after.getStatus()).isEqualTo(ItemStatus.AVAILABLE);
    }

    @Test
    @DisplayName("TC-015 存在超期未还图书时借书失败（BR-004）")
    void borrowWithOverdueLoan() {
        givenBorrowableItem();
        dataFactory.createActiveLoan(reader, TODAY.minusDays(60), TODAY.minusDays(10));

        assertThatThrownBy(this::borrow).isInstanceOf(OverdueLoanExistsException.class);
    }

    @Test
    @DisplayName("TC-016 存在未缴罚款时借书失败（BR-005）")
    void borrowWithUnpaidFine() {
        givenBorrowableItem();
        dataFactory.createUnpaidFine(reader);

        assertThatThrownBy(this::borrow).isInstanceOf(UnpaidFineExistsException.class);
    }

    @Test
    @DisplayName("TC-017 图书不可借时借书失败（BR-006）")
    void borrowUnavailableItem() {
        reader = dataFactory.createReader("张三", "20240001", ReaderType.UNDERGRADUATE);
        card = dataFactory.createCard(reader);
        item = dataFactory.addOnLoanItem(dataFactory.createTitle(ItemType.CHINESE_BOOK), ItemType.CHINESE_BOOK);

        assertThatThrownBy(this::borrow).isInstanceOf(ItemNotAvailableException.class);
    }

    @Test
    @DisplayName("TC-018 论文类不可外借")
    void borrowThesisNotAllowed() {
        reader = dataFactory.createReader("张三", "20240001", ReaderType.UNDERGRADUATE);
        card = dataFactory.createCard(reader);
        item = dataFactory.addItem(dataFactory.createTitle(ItemType.THESIS), ItemType.THESIS);

        assertThatThrownBy(this::borrow).isInstanceOf(ItemNotLoanableException.class);
    }

    @Test
    @DisplayName("TC-019 副本被他人预约时借书失败（BR-011）")
    void borrowItemReservedByOther() {
        givenBorrowableItem();
        Reader other = dataFactory.createReader("李四", "20240002", ReaderType.UNDERGRADUATE);
        reservationRepository.save(new Reservation(other.getId(), item.getBookTitle().getId(),
                LocalDateTime.now(), 1));

        assertThatThrownBy(this::borrow).isInstanceOf(ItemReservedByOtherException.class);
    }

    @Test
    @DisplayName("TC-020 普通读者不能办理借书（BR-013）")
    void borrowPermissionDenied() {
        givenBorrowableItem();

        assertThatThrownBy(() -> circulationAppService.borrowBook(
                new BorrowBookRequest(card.getCardNo(), item.getBarcode()), Operator.reader(reader.getId())))
                .isInstanceOf(PermissionDeniedException.class);
    }

    @Test
    @DisplayName("TC-021 借书失败不产生脏数据（NFR-004）")
    void borrowFailureKeepsStateConsistent() {
        givenBorrowableItem();
        dataFactory.createUnpaidFine(reader);
        long before = loanRepository.countActiveByReaderId(reader.getId());

        assertThatThrownBy(this::borrow).isInstanceOf(UnpaidFineExistsException.class);

        assertThat(loanRepository.countActiveByReaderId(reader.getId())).isEqualTo(before);
        LibraryItem after = libraryItemRepository.findByBarcode(item.getBarcode()).orElseThrow();
        assertThat(after.getStatus()).isEqualTo(ItemStatus.AVAILABLE);
    }

    @Test
    @DisplayName("借阅人正是队首预约者时，预约置为 FULFILLED")
    void headReservationFulfilled() {
        givenBorrowableItem();
        Reservation reservation = reservationRepository.save(
                new Reservation(reader.getId(), item.getBookTitle().getId(), LocalDateTime.now(), 1));

        LoanResponse response = borrow();

        assertThat(response.loanId()).isNotNull();
        assertThat(reservationRepository.findById(reservation.getId()).orElseThrow().getStatus().name())
                .isEqualTo("FULFILLED");
    }
}
