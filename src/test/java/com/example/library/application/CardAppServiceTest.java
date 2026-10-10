package com.example.library.application;

import com.example.library.domain.model.reader.BorrowCard;
import com.example.library.domain.model.reader.CardStatus;
import com.example.library.domain.model.reader.Reader;
import com.example.library.domain.model.reader.ReaderType;
import com.example.library.domain.repository.BorrowCardRepository;
import com.example.library.exception.CardAlreadyExistsException;
import com.example.library.exception.CardHasActiveLoansException;
import com.example.library.exception.CardInvalidException;
import com.example.library.exception.PermissionDeniedException;
import com.example.library.exception.ReaderNotFoundException;
import com.example.library.presentation.dto.CardResponse;
import com.example.library.support.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TASK-007 借阅证管理测试（TC-002 ~ TC-005、TC-047）。
 */
@SpringBootTest
@Transactional
class CardAppServiceTest {

    @Autowired
    private CardAppService cardAppService;

    @Autowired
    private BorrowCardRepository borrowCardRepository;

    @Autowired
    private TestDataFactory dataFactory;

    private final Operator admin = Operator.admin(1L);

    @Test
    @DisplayName("TC-002 办理借阅证成功，证号符合 CARD+年份+6位序号")
    void issueCardSuccess() {
        Reader reader = dataFactory.createReader("张三", "20240001", ReaderType.UNDERGRADUATE);

        CardResponse response = cardAppService.issueCard(reader.getId(), admin);

        assertThat(response.cardNo()).matches("^CARD\\d{4}\\d{6}$");
        assertThat(response.ownerName()).isEqualTo("张三");
        assertThat(response.department()).isEqualTo("计算机学院");
        assertThat(response.status()).isEqualTo("VALID");
    }

    @Test
    @DisplayName("TC-003 读者不存在时办证失败")
    void issueCardReaderNotFound() {
        assertThatThrownBy(() -> cardAppService.issueCard(9999L, admin))
                .isInstanceOf(ReaderNotFoundException.class);
    }

    @Test
    @DisplayName("TC-004 同一读者重复办证失败（BR-001）")
    void issueCardDuplicate() {
        Reader reader = dataFactory.createReader("张三", "20240001", ReaderType.UNDERGRADUATE);
        cardAppService.issueCard(reader.getId(), admin);

        assertThatThrownBy(() -> cardAppService.issueCard(reader.getId(), admin))
                .isInstanceOf(CardAlreadyExistsException.class);
    }

    @Test
    @DisplayName("TC-005 非系统管理员办证失败（BR-014）")
    void issueCardPermissionDenied() {
        Reader reader = dataFactory.createReader("张三", "20240001", ReaderType.UNDERGRADUATE);

        assertThatThrownBy(() -> cardAppService.issueCard(reader.getId(), Operator.librarian(2L)))
                .isInstanceOf(PermissionDeniedException.class);
        assertThatThrownBy(() -> cardAppService.issueCard(reader.getId(), Operator.reader(reader.getId())))
                .isInstanceOf(PermissionDeniedException.class);
    }

    @Test
    @DisplayName("TC-047 注销借阅证成功，状态置为 REVOKED")
    void revokeCardSuccess() {
        Reader reader = dataFactory.createReader("张三", "20240001", ReaderType.UNDERGRADUATE);
        CardResponse card = cardAppService.issueCard(reader.getId(), admin);

        cardAppService.revokeCard(card.cardNo(), admin);

        BorrowCard revoked = borrowCardRepository.findByCardNo(card.cardNo()).orElseThrow();
        assertThat(revoked.getStatus()).isEqualTo(CardStatus.REVOKED);
        assertThat(revoked.isValid()).isFalse();
    }

    @Test
    @DisplayName("TC-047 存在未还借阅时注销失败（FR-003 验收标准 1）")
    void revokeCardWithActiveLoans() {
        Reader reader = dataFactory.createReader("张三", "20240001", ReaderType.UNDERGRADUATE);
        BorrowCard card = dataFactory.createCard(reader);
        var title = dataFactory.createTitle(com.example.library.domain.model.catalog.ItemType.CHINESE_BOOK);
        var item = dataFactory.addItem(title, com.example.library.domain.model.catalog.ItemType.CHINESE_BOOK);
        dataFactory.createLoan(card, item, LocalDate.now(), LocalDate.now().plusDays(30));

        assertThatThrownBy(() -> cardAppService.revokeCard(card.getCardNo(), admin))
                .isInstanceOf(CardHasActiveLoansException.class);
    }

    @Test
    @DisplayName("注销不存在的借阅证失败")
    void revokeUnknownCard() {
        assertThatThrownBy(() -> cardAppService.revokeCard("CARD2026000099", admin))
                .isInstanceOf(CardInvalidException.class);
    }
}
