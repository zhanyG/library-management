package com.example.library.presentation;

import com.example.library.domain.model.catalog.BookTitle;
import com.example.library.domain.model.catalog.ItemType;
import com.example.library.domain.model.catalog.LibraryItem;
import com.example.library.domain.model.reader.BorrowCard;
import com.example.library.domain.model.reader.Reader;
import com.example.library.domain.model.reader.ReaderType;
import com.example.library.domain.model.reservation.Reservation;
import com.example.library.domain.repository.ReservationRepository;
import com.example.library.support.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * TASK-014 REST 端到端测试：验证接口路径、HTTP 状态码、错误码与权限（14-api-spec.md）。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiEndToEndTest {

    private static final String LIBRARIAN_ID = "7";
    private static final String LIBRARIAN_ROLE = "LIBRARIAN";
    private static final String ADMIN_ROLE = "SYSTEM_ADMIN";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestDataFactory dataFactory;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private com.example.library.domain.repository.BorrowCardRepository borrowCardRepository;

    @Test
    @DisplayName("TC-001 注册读者返回 201")
    void registerReader() throws Exception {
        mockMvc.perform(post("/api/readers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"张三","idCardNo":"20240001","phone":"13800000000",
                                 "department":"计算机学院","readerType":"UNDERGRADUATE","studentNo":"S2024001"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.readerId").isNumber())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("注册缺少必填字段返回 400 校验错误")
    void registerReaderValidationFails() throws Exception {
        mockMvc.perform(post("/api/readers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"张三\",\"department\":\"计算机学院\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("TC-002 系统管理员办理借阅证返回 201，证书号符合规范；TC-005 非管理员返回 403")
    void issueBorrowCard() throws Exception {
        Reader reader = dataFactory.createReader("张三", "20240001", ReaderType.UNDERGRADUATE);

        mockMvc.perform(post("/api/admin/borrow-cards")
                        .header("X-Operator-Id", "1")
                        .header("X-Operator-Role", ADMIN_ROLE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"readerId\":" + reader.getId() + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.cardNo").value(org.hamcrest.Matchers.matchesPattern("^CARD\\d{4}\\d{6}$")))
                .andExpect(jsonPath("$.data.status").value("VALID"));

        Reader another = dataFactory.createReader("李四", "20240002", ReaderType.UNDERGRADUATE);
        mockMvc.perform(post("/api/admin/borrow-cards")
                        .header("X-Operator-Id", LIBRARIAN_ID)
                        .header("X-Operator-Role", LIBRARIAN_ROLE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"readerId\":" + another.getId() + "}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PERMISSION_DENIED"));
    }

    @Test
    @DisplayName("TC-012 借书端到端返回 200 与应还日期")
    void borrowBook() throws Exception {
        Reader reader = dataFactory.createReader("张三", "20240001", ReaderType.UNDERGRADUATE);
        BorrowCard card = dataFactory.createCard(reader);
        LibraryItem item = dataFactory.addItem(dataFactory.createTitle(ItemType.CHINESE_BOOK),
                ItemType.CHINESE_BOOK);

        mockMvc.perform(post("/api/circulation/borrow")
                        .header("X-Operator-Id", LIBRARIAN_ID)
                        .header("X-Operator-Role", LIBRARIAN_ROLE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cardNo\":\"" + card.getCardNo() + "\",\"barcode\":\"" + item.getBarcode() + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.status").value("BORROWED"))
                .andExpect(jsonPath("$.data.dueDate").value(LocalDate.now().plusDays(30).toString()));
    }

    @Test
    @DisplayName("TC-013 借阅证无效借书返回 400 CARD_INVALID")
    void borrowWithInvalidCard() throws Exception {
        Reader reader = dataFactory.createReader("张三", "20240001", ReaderType.UNDERGRADUATE);
        BorrowCard card = dataFactory.createCard(reader);
        card.revoke();
        borrowCardRepository.save(card);
        LibraryItem item = dataFactory.addItem(dataFactory.createTitle(ItemType.CHINESE_BOOK),
                ItemType.CHINESE_BOOK);

        mockMvc.perform(post("/api/circulation/borrow")
                        .header("X-Operator-Id", LIBRARIAN_ID)
                        .header("X-Operator-Role", LIBRARIAN_ROLE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cardNo\":\"" + card.getCardNo() + "\",\"barcode\":\"" + item.getBarcode() + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CARD_INVALID"))
                .andExpect(jsonPath("$.traceId").exists());
    }

    @Test
    @DisplayName("TC-020 普通读者办理借书返回 403")
    void readerCannotBorrow() throws Exception {
        Reader reader = dataFactory.createReader("张三", "20240001", ReaderType.UNDERGRADUATE);
        BorrowCard card = dataFactory.createCard(reader);
        LibraryItem item = dataFactory.addItem(dataFactory.createTitle(ItemType.CHINESE_BOOK),
                ItemType.CHINESE_BOOK);

        mockMvc.perform(post("/api/circulation/borrow")
                        .header("X-Operator-Id", reader.getId().toString())
                        .header("X-Operator-Role", "READER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cardNo\":\"" + card.getCardNo() + "\",\"barcode\":\"" + item.getBarcode() + "\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PERMISSION_DENIED"));
    }

    @Test
    @DisplayName("TC-022 还书端到端返回 200，未超期无罚款")
    void returnBook() throws Exception {
        Reader reader = dataFactory.createReader("张三", "20240001", ReaderType.UNDERGRADUATE);
        BorrowCard card = dataFactory.createCard(reader);
        LibraryItem item = dataFactory.addOnLoanItem(dataFactory.createTitle(ItemType.CHINESE_BOOK),
                ItemType.CHINESE_BOOK);
        dataFactory.createLoan(card, item, LocalDate.now().minusDays(3), LocalDate.now().plusDays(27));

        mockMvc.perform(post("/api/circulation/return")
                        .header("X-Operator-Id", LIBRARIAN_ID)
                        .header("X-Operator-Role", LIBRARIAN_ROLE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"barcode\":\"" + item.getBarcode() + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.overdueDays").value(0))
                .andExpect(jsonPath("$.data.fineAmount").value(0.0))
                .andExpect(jsonPath("$.data.fineStatus").value("NONE"));
    }

    @Test
    @DisplayName("TC-024 归还未在借副本返回 400 ITEM_NOT_ON_LOAN")
    void returnItemNotOnLoan() throws Exception {
        LibraryItem item = dataFactory.addItem(dataFactory.createTitle(ItemType.CHINESE_BOOK),
                ItemType.CHINESE_BOOK);

        mockMvc.perform(post("/api/circulation/return")
                        .header("X-Operator-Id", LIBRARIAN_ID)
                        .header("X-Operator-Role", LIBRARIAN_ROLE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"barcode\":\"" + item.getBarcode() + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ITEM_NOT_ON_LOAN"));
    }

    @Test
    @DisplayName("TC-038/TC-040 借阅查询：本人 200、他人 403")
    void queryLoans() throws Exception {
        Reader reader = dataFactory.createReader("张三", "20240001", ReaderType.UNDERGRADUATE);
        Reader other = dataFactory.createReader("李四", "20240002", ReaderType.UNDERGRADUATE);
        BorrowCard card = dataFactory.createCard(reader);
        LibraryItem item = dataFactory.addOnLoanItem(dataFactory.createTitle(ItemType.CHINESE_BOOK),
                ItemType.CHINESE_BOOK);
        dataFactory.createLoan(card, item, LocalDate.now().minusDays(5), LocalDate.now().plusDays(25));

        mockMvc.perform(get("/api/readers/{readerId}/loans", reader.getId())
                        .header("X-Operator-Id", reader.getId().toString())
                        .header("X-Operator-Role", "READER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status").value("BORROWED"))
                .andExpect(jsonPath("$.data[0].overdue").value(false));

        mockMvc.perform(get("/api/readers/{readerId}/loans", reader.getId())
                        .header("X-Operator-Id", other.getId().toString())
                        .header("X-Operator-Role", "READER"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PERMISSION_DENIED"));
    }

    @Test
    @DisplayName("TC-050 目录查询返回可借副本数")
    void searchCatalog() throws Exception {
        BookTitle title = dataFactory.createTitle(ItemType.CHINESE_BOOK);
        dataFactory.addItem(title, ItemType.CHINESE_BOOK);
        dataFactory.addOnLoanItem(title, ItemType.CHINESE_BOOK);

        mockMvc.perform(get("/api/catalog/titles").param("keyword", title.getName()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].totalItems").value(2))
                .andExpect(jsonPath("$.data[0].availableItems").value(1));
    }

    @Test
    @DisplayName("TC-007 重复 ISBN 返回 409 TITLE_ALREADY_EXISTS")
    void duplicateIsbn() throws Exception {
        BookTitle title = dataFactory.createTitle(ItemType.CHINESE_BOOK);

        mockMvc.perform(post("/api/admin/book-titles")
                        .header("X-Operator-Id", "1")
                        .header("X-Operator-Role", ADMIN_ROLE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"重复题名\",\"isbn\":\"" + title.getIsbn()
                                + "\",\"itemType\":\"CHINESE_BOOK\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TITLE_ALREADY_EXISTS"));
    }

    @Test
    @DisplayName("TC-032/TC-033 预约端到端 201，重复预约 409")
    void reserveBook() throws Exception {
        Reader reader = dataFactory.createReader("张三", "20240001", ReaderType.UNDERGRADUATE);
        BookTitle title = dataFactory.createTitle(ItemType.CHINESE_BOOK);
        String body = "{\"readerId\":" + reader.getId() + ",\"titleId\":" + title.getId() + "}";

        mockMvc.perform(post("/api/reservations")
                        .header("X-Operator-Id", reader.getId().toString())
                        .header("X-Operator-Role", "READER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.queuePosition").value(1))
                .andExpect(jsonPath("$.data.status").value("WAITING"));

        mockMvc.perform(post("/api/reservations")
                        .header("X-Operator-Id", reader.getId().toString())
                        .header("X-Operator-Role", "READER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_RESERVATION"));
    }

    @Test
    @DisplayName("TC-049 取消本人预约 200，取消不存在预约 404")
    void cancelReservation() throws Exception {
        Reader reader = dataFactory.createReader("张三", "20240001", ReaderType.UNDERGRADUATE);
        BookTitle title = dataFactory.createTitle(ItemType.CHINESE_BOOK);
        Reservation reservation = reservationRepository.save(
                new Reservation(reader.getId(), title.getId(), LocalDateTime.now(), 1));

        mockMvc.perform(delete("/api/reservations/{id}", reservation.getId())
                        .header("X-Operator-Id", reader.getId().toString())
                        .header("X-Operator-Role", "READER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"));

        mockMvc.perform(delete("/api/reservations/{id}", 9999L)
                        .header("X-Operator-Id", reader.getId().toString())
                        .header("X-Operator-Role", "READER"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESERVATION_NOT_FOUND"));
    }

    @Test
    @DisplayName("TC-042 非法规则值返回 422 INVALID_POLICY_VALUE")
    void invalidPolicyValue() throws Exception {
        mockMvc.perform(put("/api/admin/borrow-policies/UNDERGRADUATE")
                        .header("X-Operator-Id", "1")
                        .header("X-Operator-Role", ADMIN_ROLE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"maxLoanCount\":0,\"loanPeriodDays\":0}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("INVALID_POLICY_VALUE"));
    }

    @Test
    @DisplayName("TC-041 修改借阅规则接口返回 200")
    void updateBorrowPolicy() throws Exception {
        mockMvc.perform(put("/api/admin/borrow-policies/UNDERGRADUATE")
                        .header("X-Operator-Id", "1")
                        .header("X-Operator-Role", ADMIN_ROLE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"maxLoanCount\":6,\"loanPeriodDays\":45}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"));
    }

    @Test
    @DisplayName("权限矩阵：图书管理员不能添加图书标题")
    void librarianCannotMaintainCatalog() throws Exception {
        mockMvc.perform(post("/api/admin/book-titles")
                        .header("X-Operator-Id", LIBRARIAN_ID)
                        .header("X-Operator-Role", LIBRARIAN_ROLE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"新书\",\"isbn\":\"978-7-000-00000-0\",\"itemType\":\"CHINESE_BOOK\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PERMISSION_DENIED"));
    }
}
