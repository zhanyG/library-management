package com.example.library.application;

import com.example.library.domain.model.catalog.ItemType;
import com.example.library.domain.model.reader.Reader;
import com.example.library.domain.model.reader.ReaderType;
import com.example.library.presentation.dto.BorrowBookRequest;
import com.example.library.presentation.dto.FineResponse;
import com.example.library.exception.PermissionDeniedException;
import com.example.library.support.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TASK-013 罚款查询与缴纳集成测试（FR-015、BR-005）。
 */
@SpringBootTest
@Transactional
class FineAppServiceTest {

    @Autowired
    private FineAppService fineAppService;

    @Autowired
    private CirculationAppService circulationAppService;

    @Autowired
    private TestDataFactory dataFactory;

    @Test
    @DisplayName("读者查询本人罚款成功，金额与状态正确")
    void listOwnFines() {
        Reader reader = dataFactory.createReader("张三", "20240001", ReaderType.UNDERGRADUATE);
        dataFactory.createUnpaidFine(reader);

        List<FineResponse> fines = fineAppService.listFines(reader.getId(), Operator.reader(reader.getId()));

        assertThat(fines).singleElement().satisfies(fine -> {
            assertThat(fine.amount()).isEqualByComparingTo("5.00");
            assertThat(fine.currency()).isEqualTo("CNY");
            assertThat(fine.status()).isEqualTo("UNPAID");
        });
    }

    @Test
    @DisplayName("读者查询他人罚款失败（BR-015）")
    void listOthersFinesDenied() {
        Reader owner = dataFactory.createReader("张三", "20240001", ReaderType.UNDERGRADUATE);
        Reader other = dataFactory.createReader("李四", "20240002", ReaderType.UNDERGRADUATE);

        assertThatThrownBy(() -> fineAppService.listFines(owner.getId(), Operator.reader(other.getId())))
                .isInstanceOf(PermissionDeniedException.class);
    }

    @Test
    @DisplayName("缴纳罚款后状态为 PAID，且不再阻止借书（BR-005）")
    void payFineUnblocksBorrowing() {
        Reader reader = dataFactory.createReader("张三", "20240001", ReaderType.UNDERGRADUATE);
        var card = dataFactory.createCard(reader);
        var item = dataFactory.addItem(dataFactory.createTitle(ItemType.CHINESE_BOOK), ItemType.CHINESE_BOOK);
        FineResponse fine = fineAppService.listFines(reader.getId(), Operator.reader(reader.getId()))
                .stream().findFirst().orElse(null);
        dataFactory.createUnpaidFine(reader);

        assertThat(fine).isNull();

        FineResponse unpaid = fineAppService.listFines(reader.getId(), Operator.librarian(7L)).get(0);
        assertThatThrownBy(() -> circulationAppService.borrowBook(
                new BorrowBookRequest(card.getCardNo(), item.getBarcode()), Operator.librarian(7L)))
                .isInstanceOf(com.example.library.exception.UnpaidFineExistsException.class);

        FineResponse paid = fineAppService.pay(unpaid.fineId(), Operator.librarian(7L));

        assertThat(paid.status()).isEqualTo("PAID");
        assertThat(circulationAppService.borrowBook(
                new BorrowBookRequest(card.getCardNo(), item.getBarcode()), Operator.librarian(7L)).loanId())
                .isNotNull();
    }

    @Test
    @DisplayName("普通读者不能登记缴纳罚款")
    void readerCannotPayFine() {
        Reader reader = dataFactory.createReader("张三", "20240001", ReaderType.UNDERGRADUATE);
        var fineRecord = dataFactory.createUnpaidFine(reader);

        assertThatThrownBy(() -> fineAppService.pay(fineRecord.getId(), Operator.reader(reader.getId())))
                .isInstanceOf(PermissionDeniedException.class);
    }

    @Test
    @DisplayName("超期还书生成的罚款金额可查询到")
    void fineFromOverdueReturnIsQueryable() {
        Reader reader = dataFactory.createReader("张三", "20240001", ReaderType.UNDERGRADUATE);
        var card = dataFactory.createCard(reader);
        var item = dataFactory.addOnLoanItem(dataFactory.createTitle(ItemType.CHINESE_BOOK),
                ItemType.CHINESE_BOOK);
        dataFactory.createLoan(card, item, LocalDate.now().minusDays(30), LocalDate.now().minusDays(10));

        circulationAppService.returnBook(new com.example.library.presentation.dto.ReturnBookRequest(
                item.getBarcode()), Operator.librarian(7L));

        List<FineResponse> fines = fineAppService.listFines(reader.getId(), Operator.librarian(7L));

        assertThat(fines).singleElement()
                .satisfies(fine -> assertThat(fine.amount()).isEqualByComparingTo("1.40"));
    }
}
