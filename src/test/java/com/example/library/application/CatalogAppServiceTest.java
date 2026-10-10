package com.example.library.application;

import com.example.library.domain.model.catalog.BookTitle;
import com.example.library.domain.model.catalog.ItemType;
import com.example.library.domain.model.catalog.LibraryItem;
import com.example.library.domain.repository.LibraryItemRepository;
import com.example.library.exception.ItemOnLoanCannotDeleteException;
import com.example.library.exception.PermissionDeniedException;
import com.example.library.exception.TitleAlreadyExistsException;
import com.example.library.exception.TitleHasActiveLoansException;
import com.example.library.presentation.dto.AddItemRequest;
import com.example.library.presentation.dto.CreateTitleRequest;
import com.example.library.presentation.dto.ItemResponse;
import com.example.library.presentation.dto.TitleResponse;
import com.example.library.support.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TASK-008 题名与馆藏维护测试（TC-006 ~ TC-011、TC-048、TC-050）。
 */
@SpringBootTest
@Transactional
class CatalogAppServiceTest {

    @Autowired
    private CatalogAppService catalogAppService;

    @Autowired
    private LibraryItemRepository libraryItemRepository;

    @Autowired
    private TestDataFactory dataFactory;

    private final Operator admin = Operator.admin(1L);

    private CreateTitleRequest titleRequest(String isbn, ItemType itemType) {
        return new CreateTitleRequest("软件体系结构", "某人", isbn, "机械工业出版社", 2024, itemType);
    }

    @Test
    @DisplayName("TC-006 添加图书标题成功")
    void createTitle() {
        TitleResponse response = catalogAppService.createTitle(
                titleRequest("978-7-111-00001-0", ItemType.CHINESE_BOOK), admin);

        assertThat(response.titleId()).isNotNull();
        assertThat(response.loanable()).isTrue();
        assertThat(response.totalItems()).isZero();
    }

    @Test
    @DisplayName("TC-007 重复 ISBN 添加失败")
    void duplicateIsbn() {
        String isbn = "978-7-111-00002-0";
        catalogAppService.createTitle(titleRequest(isbn, ItemType.CHINESE_BOOK), admin);

        assertThatThrownBy(() -> catalogAppService.createTitle(titleRequest(isbn, ItemType.CHINESE_BOOK), admin))
                .isInstanceOf(TitleAlreadyExistsException.class);
    }

    @Test
    @DisplayName("TC-008 论文类题目标记为不可外借")
    void thesisNotLoanable() {
        TitleResponse response = catalogAppService.createTitle(
                titleRequest("978-7-111-00003-0", ItemType.THESIS), admin);

        assertThat(response.loanable()).isFalse();
    }

    @Test
    @DisplayName("TC-009 添加馆藏副本成功，条码符合 ITEM+年份+6位序号")
    void addItem() {
        TitleResponse title = catalogAppService.createTitle(
                titleRequest("978-7-111-00004-0", ItemType.CHINESE_BOOK), admin);

        ItemResponse response = catalogAppService.addItem(new AddItemRequest(title.titleId(), "A区3排"), admin);

        assertThat(response.barcode()).matches("^ITEM\\d{4}\\d{6}$");
        assertThat(response.status()).isEqualTo("AVAILABLE");
        assertThat(response.titleName()).isEqualTo(title.name());
    }

    @Test
    @DisplayName("TC-010 普通读者添加馆藏副本失败（BR-014）")
    void addItemPermissionDenied() {
        TitleResponse title = catalogAppService.createTitle(
                titleRequest("978-7-111-00005-0", ItemType.CHINESE_BOOK), admin);

        assertThatThrownBy(() -> catalogAppService.addItem(new AddItemRequest(title.titleId(), null),
                Operator.reader(1L)))
                .isInstanceOf(PermissionDeniedException.class);
    }

    @Test
    @DisplayName("TC-011 删除在借副本失败")
    void removeOnLoanItem() {
        BookTitle title = dataFactory.createTitle(ItemType.CHINESE_BOOK);
        LibraryItem item = dataFactory.addOnLoanItem(title, ItemType.CHINESE_BOOK);

        assertThatThrownBy(() -> catalogAppService.removeItem(item.getBarcode(), admin))
                .isInstanceOf(ItemOnLoanCannotDeleteException.class);
    }

    @Test
    @DisplayName("TC-048 题名下存在在借副本时删除题名失败")
    void deleteTitleWithActiveLoan() {
        BookTitle title = dataFactory.createTitle(ItemType.CHINESE_BOOK);
        dataFactory.addOnLoanItem(title, ItemType.CHINESE_BOOK);

        assertThatThrownBy(() -> catalogAppService.deleteTitle(title.getId(), admin))
                .isInstanceOf(TitleHasActiveLoansException.class);
    }

    @Test
    @DisplayName("删除无在借副本的题名成功")
    void deleteTitleSuccess() {
        BookTitle title = dataFactory.createTitle(ItemType.CHINESE_BOOK);
        dataFactory.addItem(title, ItemType.CHINESE_BOOK);

        catalogAppService.deleteTitle(title.getId(), admin);

        assertThat(catalogAppService.searchTitles(title.getName())).isEmpty();
    }

    @Test
    @DisplayName("TC-050 检索返回题名与可借副本数量")
    void searchTitles() {
        BookTitle title = dataFactory.createTitle(ItemType.CHINESE_BOOK);
        LibraryItem available = dataFactory.addItem(title, ItemType.CHINESE_BOOK);
        dataFactory.addOnLoanItem(title, ItemType.CHINESE_BOOK);

        List<TitleResponse> found = catalogAppService.searchTitles(title.getIsbn());

        assertThat(found).singleElement().satisfies(result -> {
            assertThat(result.totalItems()).isEqualTo(2);
            assertThat(result.availableItems()).isEqualTo(1);
        });
        assertThat(libraryItemRepository.findByBarcode(available.getBarcode())).isPresent();
    }
}
