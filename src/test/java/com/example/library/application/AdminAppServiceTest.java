package com.example.library.application;

import com.example.library.domain.model.catalog.ItemType;
import com.example.library.domain.model.reader.Reader;
import com.example.library.domain.model.reader.ReaderStatus;
import com.example.library.domain.model.reader.ReaderType;
import com.example.library.domain.model.reservation.Reservation;
import com.example.library.domain.repository.LibrarianRepository;
import com.example.library.presentation.dto.AddLibrarianRequest;
import com.example.library.presentation.dto.ReturnBookRequest;
import com.example.library.presentation.dto.UpdateBorrowPolicyRequest;
import com.example.library.presentation.dto.UpdateFineRuleRequest;
import com.example.library.exception.BusinessException;
import com.example.library.exception.InvalidPolicyValueException;
import com.example.library.exception.PermissionDeniedException;
import com.example.library.support.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TASK-012 管理员维护与规则生效集成测试（TC-041 ~ TC-043、BR-014）。
 */
@SpringBootTest
@Transactional
class AdminAppServiceTest {

    @Autowired
    private AdminAppService adminAppService;

    @Autowired
    private CirculationAppService circulationAppService;

    @Autowired
    private LibrarianRepository librarianRepository;

    @Autowired
    private ReservationAppService reservationAppService;

    @Autowired
    private com.example.library.domain.repository.ReservationRepository reservationRepository;

    @Autowired
    private TestDataFactory dataFactory;

    private final Operator admin = Operator.admin(1L);

    @Test
    @DisplayName("添加图书管理员成功，重复工号失败")
    void addLibrarian() {
        adminAppService.addLibrarian(new AddLibrarianRequest("L001", "馆员甲"), admin);

        assertThat(librarianRepository.findByStaffNo("L001")).isPresent();
        assertThatThrownBy(() -> adminAppService.addLibrarian(new AddLibrarianRequest("L001", "馆员乙"), admin))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("已存在");
    }

    @Test
    @DisplayName("删除（停用）图书管理员后不可办理借还")
    void removeLibrarianDisablesAccount() {
        adminAppService.addLibrarian(new AddLibrarianRequest("L002", "馆员乙"), admin);

        adminAppService.removeLibrarian("L002", admin);

        assertThat(librarianRepository.findByStaffNo("L002").orElseThrow().getStatus())
                .isEqualTo(ReaderStatus.INACTIVE);
    }

    @Test
    @DisplayName("TC-041 修改借阅规则后对新借书立即生效（FR-016）")
    void updateBorrowPolicyTakesEffect() {
        adminAppService.updateBorrowPolicy(ReaderType.UNDERGRADUATE,
                new UpdateBorrowPolicyRequest(6, 45), admin);

        Reader reader = dataFactory.createReader("张三", "20240001", ReaderType.UNDERGRADUATE);
        var card = dataFactory.createCard(reader);
        var item = dataFactory.addItem(dataFactory.createTitle(ItemType.CHINESE_BOOK), ItemType.CHINESE_BOOK);

        var response = circulationAppService.borrowBook(
                new com.example.library.presentation.dto.BorrowBookRequest(card.getCardNo(), item.getBarcode()),
                Operator.librarian(7L));

        assertThat(response.dueDate()).isEqualTo(LocalDate.now().plusDays(45));
    }

    @Test
    @DisplayName("TC-042 非法规则值被拒绝（FR-016）")
    void invalidPolicyValueRejected() {
        assertThatThrownBy(() -> adminAppService.updateBorrowPolicy(ReaderType.UNDERGRADUATE,
                new UpdateBorrowPolicyRequest(-1, 30), admin))
                .isInstanceOf(InvalidPolicyValueException.class);

        assertThatThrownBy(() -> adminAppService.updateFineRule(ItemType.CHINESE_BOOK,
                new UpdateFineRuleRequest(-1, new BigDecimal("0.20"), new BigDecimal("50.00")), admin))
                .isInstanceOf(InvalidPolicyValueException.class);
    }

    @Test
    @DisplayName("TC-043 修改罚款规则后对新还书立即生效")
    void updateFineRuleTakesEffect() {
        adminAppService.updateFineRule(ItemType.CHINESE_BOOK,
                new UpdateFineRuleRequest(0, new BigDecimal("1.00"), new BigDecimal("30.00")), admin);

        Reader reader = dataFactory.createReader("张三", "20240001", ReaderType.UNDERGRADUATE);
        var card = dataFactory.createCard(reader);
        var item = dataFactory.addOnLoanItem(dataFactory.createTitle(ItemType.CHINESE_BOOK),
                ItemType.CHINESE_BOOK);
        dataFactory.createLoan(card, item, LocalDate.now().minusDays(20), LocalDate.now().minusDays(5));

        var response = circulationAppService.returnBook(new ReturnBookRequest(item.getBarcode()),
                Operator.librarian(7L));

        assertThat(response.overdueDays()).isEqualTo(5);
        assertThat(response.fineAmount()).isEqualByComparingTo("5.00");
    }

    @Test
    @DisplayName("非系统管理员执行维护失败（BR-014）")
    void adminOperationsDeniedForOthers() {
        Operator librarian = Operator.librarian(7L);

        assertThatThrownBy(() -> adminAppService.addLibrarian(new AddLibrarianRequest("L003", "馆员丙"), librarian))
                .isInstanceOf(PermissionDeniedException.class);
        assertThatThrownBy(() -> adminAppService.updateBorrowPolicy(ReaderType.UNDERGRADUATE,
                new UpdateBorrowPolicyRequest(5, 30), librarian))
                .isInstanceOf(PermissionDeniedException.class);
        assertThatThrownBy(() -> adminAppService.updateFineRule(ItemType.CHINESE_BOOK,
                new UpdateFineRuleRequest(3, new BigDecimal("0.20"), new BigDecimal("50.00")), librarian))
                .isInstanceOf(PermissionDeniedException.class);
    }

    @Test
    @DisplayName("图书管理员不能取消读者预约（仅本人或系统管理员）")
    void librarianCannotCancelReservation() {
        Reader reader = dataFactory.createReader("张三", "20240001", ReaderType.UNDERGRADUATE);
        var title = dataFactory.createTitle(ItemType.CHINESE_BOOK);
        Reservation reservation = reservationRepository.save(
                new Reservation(reader.getId(), title.getId(), LocalDateTime.now(), 1));

        assertThatThrownBy(() -> reservationAppService.cancel(reservation.getId(), Operator.librarian(7L)))
                .isInstanceOf(PermissionDeniedException.class);
    }
}
