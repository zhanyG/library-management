package com.example.library.application;

import com.example.library.domain.model.catalog.LibraryItem;
import com.example.library.domain.model.circulation.Loan;
import com.example.library.domain.repository.LibraryItemRepository;
import com.example.library.presentation.dto.LoanQueryResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 借阅视图装配器：把 Loan 聚合为借阅信息响应（FR-013）。
 */
@Component
public class LoanViewAssembler {

    private final LibraryItemRepository libraryItemRepository;

    public LoanViewAssembler(LibraryItemRepository libraryItemRepository) {
        this.libraryItemRepository = libraryItemRepository;
    }

    public LoanQueryResponse toQueryResponse(Loan loan, LocalDate today) {
        LibraryItem item = libraryItemRepository.findById(loan.getItemId()).orElse(null);
        String barcode = item == null ? null : item.getBarcode();
        String titleName = item == null || item.getBookTitle() == null ? null : item.getBookTitle().getName();
        return new LoanQueryResponse(
                loan.getId(),
                titleName,
                barcode,
                loan.getBorrowDate(),
                loan.getDueDate(),
                loan.getReturnDate(),
                loan.getStatus().name(),
                loan.isOverdue(today),
                loan.overdueDays(today));
    }
}
