package com.example.library.application;

import com.example.library.domain.model.reader.BorrowCard;
import com.example.library.domain.model.reader.Reader;
import com.example.library.domain.repository.BorrowCardRepository;
import com.example.library.domain.repository.LoanRepository;
import com.example.library.domain.repository.ReaderRepository;
import com.example.library.domain.service.NumberGenerator;
import com.example.library.exception.CardAlreadyExistsException;
import com.example.library.exception.CardHasActiveLoansException;
import com.example.library.exception.CardInvalidException;
import com.example.library.exception.ReaderNotFoundException;
import com.example.library.presentation.dto.CardResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 借阅证应用服务：办理（FR-002）与注销（FR-003），仅系统管理员可执行（BR-014）。
 */
@Service
public class CardAppService {

    private final BorrowCardRepository borrowCardRepository;
    private final ReaderRepository readerRepository;
    private final LoanRepository loanRepository;
    private final NumberGenerator numberGenerator;
    private final PermissionChecker permissionChecker;

    public CardAppService(BorrowCardRepository borrowCardRepository,
                          ReaderRepository readerRepository,
                          LoanRepository loanRepository,
                          NumberGenerator numberGenerator,
                          PermissionChecker permissionChecker) {
        this.borrowCardRepository = borrowCardRepository;
        this.readerRepository = readerRepository;
        this.loanRepository = loanRepository;
        this.numberGenerator = numberGenerator;
        this.permissionChecker = permissionChecker;
    }

    /** FR-002 办理借阅证：读者必须存在，且一个读者最多一张有效证（BR-001）。 */
    @Transactional
    public CardResponse issueCard(Long readerId, Operator operator) {
        permissionChecker.assertAdmin(operator);
        Reader reader = readerRepository.findById(readerId).orElseThrow(ReaderNotFoundException::new);
        if (borrowCardRepository.existsValidCardByReaderId(readerId)) {
            throw new CardAlreadyExistsException("读者 " + reader.getName() + " 已持有有效借阅证");
        }
        BorrowCard card = new BorrowCard(numberGenerator.nextCardNo(), reader.getId(),
                reader.getName(), reader.getDepartment(), LocalDate.now());
        BorrowCard saved = borrowCardRepository.save(card);
        return toResponse(saved);
    }

    /** FR-003 注销借阅证：存在未还借阅时拒绝。 */
    @Transactional
    public void revokeCard(String cardNo, Operator operator) {
        permissionChecker.assertAdmin(operator);
        BorrowCard card = borrowCardRepository.findByCardNo(cardNo).orElseThrow(CardInvalidException::new);
        if (!card.isValid()) {
            throw new CardInvalidException("借阅证 " + cardNo + " 已失效");
        }
        if (loanRepository.countActiveByReaderId(card.getReaderId()) > 0) {
            throw new CardHasActiveLoansException("借阅证 " + cardNo + " 下存在未还借阅记录");
        }
        card.revoke();
        borrowCardRepository.save(card);
    }

    private CardResponse toResponse(BorrowCard card) {
        return new CardResponse(card.getCardNo(), card.getOwnerName(), card.getDepartment(),
                card.getIssueDate(), card.getStatus().name());
    }
}
