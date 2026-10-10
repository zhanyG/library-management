package com.example.library.application;

import com.example.library.domain.model.reader.BorrowCard;
import com.example.library.domain.repository.BorrowCardRepository;
import com.example.library.domain.repository.LoanRepository;
import com.example.library.domain.service.CirculationService;
import com.example.library.domain.service.ReturnResult;
import com.example.library.exception.CardInvalidException;
import com.example.library.presentation.dto.BorrowBookRequest;
import com.example.library.presentation.dto.LoanQueryResponse;
import com.example.library.presentation.dto.LoanResponse;
import com.example.library.presentation.dto.ReturnBookRequest;
import com.example.library.presentation.dto.ReturnBookResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 流通应用服务：事务边界与权限校验（UC-101、UC-102、UC-103）。
 */
@Service
public class CirculationAppService {

    private final CirculationService circulationService;
    private final BorrowCardRepository borrowCardRepository;
    private final LoanRepository loanRepository;
    private final LoanViewAssembler loanViewAssembler;
    private final PermissionChecker permissionChecker;

    public CirculationAppService(CirculationService circulationService,
                                 BorrowCardRepository borrowCardRepository,
                                 LoanRepository loanRepository,
                                 LoanViewAssembler loanViewAssembler,
                                 PermissionChecker permissionChecker) {
        this.circulationService = circulationService;
        this.borrowCardRepository = borrowCardRepository;
        this.loanRepository = loanRepository;
        this.loanViewAssembler = loanViewAssembler;
        this.permissionChecker = permissionChecker;
    }

    /** FR-011 办理借书（仅图书管理员，BR-013）。 */
    @Transactional
    public LoanResponse borrowBook(BorrowBookRequest request, Operator operator) {
        permissionChecker.assertLibrarian(operator);
        return loanViewAssembler.toLoanResponse(
                circulationService.borrowBook(request.cardNo(), request.barcode(), operator.id(), LocalDate.now()));
    }

    /** FR-012 办理还书（仅图书管理员）。 */
    @Transactional
    public ReturnBookResponse returnBook(ReturnBookRequest request, Operator operator) {
        permissionChecker.assertLibrarian(operator);
        ReturnResult result = circulationService.returnBook(request.barcode(), operator.id(), LocalDate.now());
        return new ReturnBookResponse(result.loanId(), result.returnDate(), result.overdueDays(),
                result.fineAmount().amount(), result.fineStatus());
    }

    /** UC-103 图书管理员按借阅证号查询读者借阅信息。 */
    @Transactional(readOnly = true)
    public List<LoanQueryResponse> listLoansByCard(String cardNo, Operator operator) {
        permissionChecker.assertLibrarian(operator);
        BorrowCard card = borrowCardRepository.findByCardNo(cardNo).orElseThrow(CardInvalidException::new);
        LocalDate today = LocalDate.now();
        return loanRepository.findByReaderId(card.getReaderId()).stream()
                .map(loan -> loanViewAssembler.toQueryResponse(loan, today))
                .toList();
    }
}
