package com.example.library.application;

import com.example.library.domain.model.catalog.BookTitle;
import com.example.library.domain.model.catalog.ItemType;
import com.example.library.domain.model.catalog.LibraryItem;
import com.example.library.domain.model.reader.BorrowCard;
import com.example.library.domain.model.reader.Reader;
import com.example.library.domain.model.reader.ReaderType;
import com.example.library.exception.DuplicateIdCardException;
import com.example.library.exception.PermissionDeniedException;
import com.example.library.presentation.dto.LoanQueryResponse;
import com.example.library.presentation.dto.ReaderResponse;
import com.example.library.presentation.dto.RegisterReaderRequest;
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
 * TASK-006 读者注册与借阅查询测试（TC-001、TC-038、TC-039、TC-040）。
 */
@SpringBootTest
@Transactional
class ReaderAppServiceTest {

    @Autowired
    private ReaderAppService readerAppService;

    @Autowired
    private TestDataFactory dataFactory;

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 10);

    @Test
    @DisplayName("TC-001 注册读者成功")
    void registerSuccess() {
        ReaderResponse response = readerAppService.register(new RegisterReaderRequest(
                "张三", "20240001", "13800000000", "计算机学院",
                ReaderType.UNDERGRADUATE, "S2024001", "2024", "软件工程", null, null, null));

        assertThat(response.readerId()).isNotNull();
        assertThat(response.readerType()).isEqualTo("UNDERGRADUATE");
        assertThat(response.status()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("证件号重复注册被拒绝（FR-001 验收标准 2）")
    void registerDuplicateIdCard() {
        readerAppService.register(new RegisterReaderRequest("张三", "20240001", null, "计算机学院",
                ReaderType.UNDERGRADUATE, null, null, null, null, null, null));

        assertThatThrownBy(() -> readerAppService.register(new RegisterReaderRequest("李四", "20240001", null,
                "外国语学院", ReaderType.POSTGRADUATE, null, null, null, null, null, null)))
                .isInstanceOf(DuplicateIdCardException.class);
    }

    @Test
    @DisplayName("TC-038 读者查询本人借阅信息成功")
    void readerQueriesOwnLoans() {
        Reader reader = dataFactory.createReader("张三", "20240001", ReaderType.UNDERGRADUATE);
        BorrowCard card = dataFactory.createCard(reader);
        BookTitle title = dataFactory.createTitle(ItemType.CHINESE_BOOK);
        LibraryItem item = dataFactory.addItem(title, ItemType.CHINESE_BOOK);
        dataFactory.createLoan(card, item, TODAY.minusDays(10), TODAY.plusDays(20));

        List<LoanQueryResponse> loans = readerAppService.listLoans(reader.getId(), Operator.reader(reader.getId()), TODAY);

        assertThat(loans).hasSize(1);
        assertThat(loans.get(0).titleName()).isEqualTo(title.getName());
        assertThat(loans.get(0).status()).isEqualTo("BORROWED");
        assertThat(loans.get(0).overdue()).isFalse();
    }

    @Test
    @DisplayName("TC-039 图书管理员可代查指定读者借阅信息")
    void librarianQueriesAnyReader() {
        Reader reader = dataFactory.createReader("张三", "20240001", ReaderType.UNDERGRADUATE);
        dataFactory.createCard(reader);

        List<LoanQueryResponse> loans = readerAppService.listLoans(reader.getId(), Operator.librarian(99L), TODAY);

        assertThat(loans).isEmpty();
    }

    @Test
    @DisplayName("TC-040 普通读者查询他人借阅失败（BR-015）")
    void readerCannotQueryOthers() {
        Reader owner = dataFactory.createReader("张三", "20240001", ReaderType.UNDERGRADUATE);
        Reader other = dataFactory.createReader("李四", "20240002", ReaderType.UNDERGRADUATE);

        assertThatThrownBy(() ->
                readerAppService.listLoans(owner.getId(), Operator.reader(other.getId()), TODAY))
                .isInstanceOf(PermissionDeniedException.class);
    }

    @Test
    @DisplayName("超期借阅在查询结果中标记为 overdue")
    void overdueLoanFlagged() {
        Reader reader = dataFactory.createReader("张三", "20240001", ReaderType.UNDERGRADUATE);
        BorrowCard card = dataFactory.createCard(reader);
        BookTitle title = dataFactory.createTitle(ItemType.CHINESE_BOOK);
        LibraryItem item = dataFactory.addItem(title, ItemType.CHINESE_BOOK);
        dataFactory.createLoan(card, item, TODAY.minusDays(60), TODAY.minusDays(10));

        List<LoanQueryResponse> loans = readerAppService.listLoans(reader.getId(), Operator.reader(reader.getId()), TODAY);

        assertThat(loans).singleElement().satisfies(loan -> {
            assertThat(loan.overdue()).isTrue();
            assertThat(loan.overdueDays()).isEqualTo(10);
        });
    }
}
