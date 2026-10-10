package com.example.library.application;

import com.example.library.domain.model.catalog.LibraryItem;
import com.example.library.domain.model.circulation.Loan;
import com.example.library.domain.model.reader.Reader;
import com.example.library.domain.repository.LibraryItemRepository;
import com.example.library.domain.repository.ReaderRepository;
import com.example.library.presentation.dto.LoanQueryResponse;
import com.example.library.presentation.dto.LoanResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 借阅视图装配器：把 Loan 聚合为借阅信息响应（FR-013）与借书结果响应（FR-011）。
 */
@Component
public class LoanViewAssembler {

    private final LibraryItemRepository libraryItemRepository;
    private final ReaderRepository readerRepository;

    public LoanViewAssembler(LibraryItemRepository libraryItemRepository, ReaderRepository readerRepository) {
        this.libraryItemRepository = libraryItemRepository;
        this.readerRepository = readerRepository;
    }

    public LoanQueryResponse toQueryResponse(Loan loan, LocalDate today) {
        LibraryItem item = libraryItemRepository.findById(loan.getItemId()).orElse(null);
        return new LoanQueryResponse(
                loan.getId(),
                titleNameOf(item),
                barcodeOf(item),
                loan.getBorrowDate(),
                loan.getDueDate(),
                loan.getReturnDate(),
                loan.getStatus().name(),
                loan.isOverdue(today),
                loan.overdueDays(today));
    }

    public LoanResponse toLoanResponse(Loan loan) {
        LibraryItem item = libraryItemRepository.findById(loan.getItemId()).orElse(null);
        String readerName = readerRepository.findById(loan.getReaderId()).map(Reader::getName).orElse(null);
        return new LoanResponse(
                loan.getId(),
                readerName,
                titleNameOf(item),
                barcodeOf(item),
                loan.getBorrowDate(),
                loan.getDueDate(),
                loan.getStatus().name());
    }

    private String titleNameOf(LibraryItem item) {
        return item == null || item.getBookTitle() == null ? null : item.getBookTitle().getName();
    }

    private String barcodeOf(LibraryItem item) {
        return item == null ? null : item.getBarcode();
    }
}
