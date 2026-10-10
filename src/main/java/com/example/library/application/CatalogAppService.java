package com.example.library.application;

import com.example.library.domain.model.catalog.BookTitle;
import com.example.library.domain.model.catalog.LibraryItem;
import com.example.library.domain.model.catalog.LibraryItemFactory;
import com.example.library.domain.repository.BookTitleRepository;
import com.example.library.domain.repository.LibraryItemRepository;
import com.example.library.domain.service.NumberGenerator;
import com.example.library.exception.DuplicateBarcodeException;
import com.example.library.exception.ItemNotFoundException;
import com.example.library.exception.ItemOnLoanCannotDeleteException;
import com.example.library.exception.ItemReservedByOtherException;
import com.example.library.exception.TitleAlreadyExistsException;
import com.example.library.exception.TitleHasActiveLoansException;
import com.example.library.exception.TitleNotFoundException;
import com.example.library.presentation.dto.AddItemRequest;
import com.example.library.presentation.dto.CreateTitleRequest;
import com.example.library.presentation.dto.ItemResponse;
import com.example.library.presentation.dto.TitleResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 目录应用服务：题名与馆藏副本的查询与维护（FR-006 ~ FR-010）。
 */
@Service
public class CatalogAppService {

    private final BookTitleRepository bookTitleRepository;
    private final LibraryItemRepository libraryItemRepository;
    private final NumberGenerator numberGenerator;
    private final PermissionChecker permissionChecker;

    public CatalogAppService(BookTitleRepository bookTitleRepository,
                             LibraryItemRepository libraryItemRepository,
                             NumberGenerator numberGenerator,
                             PermissionChecker permissionChecker) {
        this.bookTitleRepository = bookTitleRepository;
        this.libraryItemRepository = libraryItemRepository;
        this.numberGenerator = numberGenerator;
        this.permissionChecker = permissionChecker;
    }

    /** FR-010 查询图书：按关键字匹配题名/作者/ISBN。 */
    @Transactional(readOnly = true)
    public List<TitleResponse> searchTitles(String keyword) {
        String normalized = keyword == null ? "" : keyword.trim();
        return bookTitleRepository.searchByKeyword(normalized).stream()
                .map(this::toTitleResponse)
                .toList();
    }

    /** FR-006 添加图书标题信息（仅系统管理员）。 */
    @Transactional
    public TitleResponse createTitle(CreateTitleRequest request, Operator operator) {
        permissionChecker.assertAdmin(operator);
        if (bookTitleRepository.existsByIsbn(request.isbn())) {
            throw new TitleAlreadyExistsException("ISBN " + request.isbn() + " 已存在");
        }
        BookTitle title = new BookTitle(request.name(), request.author(), request.isbn(),
                request.publisher(), request.publishedYear(), request.itemType());
        return toTitleResponse(bookTitleRepository.save(title));
    }

    /** FR-007 删除图书标题信息：存在在借副本时拒绝。 */
    @Transactional
    public void deleteTitle(Long titleId, Operator operator) {
        permissionChecker.assertAdmin(operator);
        BookTitle title = bookTitleRepository.findById(titleId).orElseThrow(TitleNotFoundException::new);
        if (libraryItemRepository.existsOnLoanByTitleId(titleId)) {
            throw new TitleHasActiveLoansException("题名 " + title.getName() + " 下存在在借副本");
        }
        libraryItemRepository.findByTitleId(titleId).forEach(libraryItemRepository::delete);
        bookTitleRepository.delete(title);
    }

    /** FR-008 添加馆藏副本：条码由系统生成且唯一。 */
    @Transactional
    public ItemResponse addItem(AddItemRequest request, Operator operator) {
        permissionChecker.assertAdmin(operator);
        BookTitle title = bookTitleRepository.findById(request.titleId()).orElseThrow(TitleNotFoundException::new);
        String barcode = numberGenerator.nextBarcode();
        if (libraryItemRepository.existsByBarcode(barcode)) {
            throw new DuplicateBarcodeException("条码 " + barcode + " 已存在");
        }
        LibraryItem item = LibraryItemFactory.create(title.getItemType(), barcode, request.location());
        item.attachTo(title);
        LibraryItem saved = libraryItemRepository.save(item);
        return new ItemResponse(saved.getBarcode(), title.getName(), saved.getItemType().name(),
                saved.getStatus().name(), saved.getLocation());
    }

    /** FR-009 删除馆藏副本：在借副本拒绝删除，其余下架处理。 */
    @Transactional
    public void removeItem(String barcode, Operator operator) {
        permissionChecker.assertAdmin(operator);
        LibraryItem item = libraryItemRepository.findByBarcode(barcode)
                .orElseThrow(() -> new ItemNotFoundException("条码 " + barcode + " 不存在"));
        if (item.isOnLoan()) {
            throw new ItemOnLoanCannotDeleteException("副本 " + barcode + " 正在外借，不能删除");
        }
        if (item.isReserved()) {
            throw new ItemReservedByOtherException("副本 " + barcode + " 正被预约保留，不能删除");
        }
        item.withdraw();
        libraryItemRepository.save(item);
    }

    private TitleResponse toTitleResponse(BookTitle title) {
        long total = libraryItemRepository.countByTitleId(title.getId());
        long available = libraryItemRepository.countAvailableByTitleId(title.getId());
        return new TitleResponse(title.getId(), title.getName(), title.getAuthor(), title.getIsbn(),
                title.getItemType().name(), title.isLoanable(), total, available);
    }
}
