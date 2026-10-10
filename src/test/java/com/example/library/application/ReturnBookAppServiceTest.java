package com.example.library.application;

import com.example.library.domain.model.catalog.ItemStatus;
import com.example.library.domain.model.catalog.ItemType;
import com.example.library.domain.model.catalog.LibraryItem;
import com.example.library.domain.model.reader.BorrowCard;
import com.example.library.domain.model.reader.Reader;
import com.example.library.domain.model.reader.ReaderType;
import com.example.library.domain.model.reservation.Reservation;
import com.example.library.domain.repository.FineRecordRepository;
import com.example.library.domain.repository.LibraryItemRepository;
import com.example.library.domain.repository.LoanRepository;
import com.example.library.domain.repository.ReservationRepository;
import com.example.library.exception.ItemNotFoundException;
import com.example.library.exception.ItemNotOnLoanException;
import com.example.library.exception.PermissionDeniedException;
import com.example.library.presentation.dto.ReturnBookRequest;
import com.example.library.presentation.dto.ReturnBookResponse;
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
 * TASK-010 办理还书与罚款生成集成测试（TC-022 ~ TC-026、TC-028）。
 */
@SpringBootTest
@Transactional
class ReturnBookAppServiceTest {

    @Autowired
    private CirculationAppService circulationAppService;

    @Autowired
    private LibraryItemRepository libraryItemRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private FineRecordRepository fineRecordRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private TestDataFactory dataFactory;

    private final Operator librarian = Operator.librarian(7L);

    private Reader reader;

    private BorrowCard card;

    private LibraryItem item;

    private void givenOnLoanItem(ItemType itemType, LocalDate dueDate) {
        reader = dataFactory.createReader("张三", "20240001", ReaderType.UNDERGRADUATE);
        card = dataFactory.createCard(reader);
        item = dataFactory.addOnLoanItem(dataFactory.createTitle(itemType), itemType);
        dataFactory.createLoan(card, item, LocalDate.now().minusDays(40), dueDate);
    }

    private ReturnBookResponse returnBook() {
        return circulationAppService.returnBook(new ReturnBookRequest(item.getBarcode()), librarian);
    }

    @Test
    @DisplayName("TC-022 未超期还书成功：状态 RETURNED、副本 AVAILABLE、无罚款")
    void returnSuccess() {
        givenOnLoanItem(ItemType.CHINESE_BOOK, LocalDate.now().plusDays(5));

        ReturnBookResponse response = returnBook();

        assertThat(response.returnDate()).isEqualTo(LocalDate.now());
        assertThat(response.overdueDays()).isZero();
        assertThat(response.fineAmount()).isEqualByComparingTo("0.00");
        assertThat(response.fineStatus()).isEqualTo("NONE");

        assertThat(libraryItemRepository.findByBarcode(item.getBarcode()).orElseThrow().getStatus())
                .isEqualTo(ItemStatus.AVAILABLE);
        assertThat(loanRepository.countActiveByReaderId(reader.getId())).isZero();
    }

    @Test
    @DisplayName("TC-023 归还非本馆藏书失败")
    void returnUnknownItem() {
        assertThatThrownBy(() -> circulationAppService.returnBook(
                new ReturnBookRequest("ITEM2026999999"), librarian))
                .isInstanceOf(ItemNotFoundException.class);
    }

    @Test
    @DisplayName("TC-024 归还未在借副本失败")
    void returnItemNotOnLoan() {
        LibraryItem available = dataFactory.addItem(dataFactory.createTitle(ItemType.CHINESE_BOOK),
                ItemType.CHINESE_BOOK);

        assertThatThrownBy(() -> circulationAppService.returnBook(
                new ReturnBookRequest(available.getBarcode()), librarian))
                .isInstanceOf(ItemNotOnLoanException.class);
    }

    @Test
    @DisplayName("TC-025 超期还书生成罚款：中文图书免罚 3 天、每日 0.20")
    void returnOverdueCreatesFine() {
        givenOnLoanItem(ItemType.CHINESE_BOOK, LocalDate.now().minusDays(20));

        ReturnBookResponse response = returnBook();

        assertThat(response.overdueDays()).isEqualTo(20);
        assertThat(response.fineAmount()).isEqualByComparingTo("3.40");
        assertThat(response.fineStatus()).isEqualTo("UNPAID");
        assertThat(fineRecordRepository.existsUnpaidByReaderId(reader.getId())).isTrue();
    }

    @Test
    @DisplayName("TC-026 免罚期内不生成罚款")
    void returnWithinGracePeriod() {
        givenOnLoanItem(ItemType.CHINESE_BOOK, LocalDate.now().minusDays(2));

        ReturnBookResponse response = returnBook();

        assertThat(response.overdueDays()).isEqualTo(2);
        assertThat(response.fineAmount()).isEqualByComparingTo("0.00");
        assertThat(fineRecordRepository.existsUnpaidByReaderId(reader.getId())).isFalse();
    }

    @Test
    @DisplayName("TC-027 罚款受单笔上限约束（中文杂志上限 20.00）")
    void fineCappedByRule() {
        givenOnLoanItem(ItemType.CHINESE_MAGAZINE, LocalDate.now().minusDays(400));

        ReturnBookResponse response = returnBook();

        assertThat(response.fineAmount()).isEqualByComparingTo("20.00");
    }

    @Test
    @DisplayName("TC-028 有排队预约时还书后副本置为 RESERVED 并保留 7 天")
    void returnReservesItemForHeadReservation() {
        givenOnLoanItem(ItemType.CHINESE_BOOK, LocalDate.now().plusDays(3));
        Reservation reservation = reservationRepository.save(
                new Reservation(reader.getId(), item.getBookTitle().getId(), LocalDateTime.now(), 1));

        returnBook();

        assertThat(libraryItemRepository.findByBarcode(item.getBarcode()).orElseThrow().getStatus())
                .isEqualTo(ItemStatus.RESERVED);
        Reservation reloaded = reservationRepository.findById(reservation.getId()).orElseThrow();
        assertThat(reloaded.getExpireAt()).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("普通读者不能办理还书（BR-013）")
    void returnPermissionDenied() {
        givenOnLoanItem(ItemType.CHINESE_BOOK, LocalDate.now().plusDays(3));

        assertThatThrownBy(() -> circulationAppService.returnBook(
                new ReturnBookRequest(item.getBarcode()), Operator.reader(reader.getId())))
                .isInstanceOf(PermissionDeniedException.class);
    }
}
