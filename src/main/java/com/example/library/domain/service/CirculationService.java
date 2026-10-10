package com.example.library.domain.service;

import com.example.library.domain.model.catalog.LibraryItem;
import com.example.library.domain.model.circulation.BorrowPolicy;
import com.example.library.domain.model.circulation.Loan;
import com.example.library.domain.model.fine.FineRecord;
import com.example.library.domain.model.fine.FineRule;
import com.example.library.domain.model.fine.Money;
import com.example.library.domain.model.reader.BorrowCard;
import com.example.library.domain.model.reader.Reader;
import com.example.library.domain.model.reservation.Reservation;
import com.example.library.domain.repository.BorrowCardRepository;
import com.example.library.domain.repository.BorrowPolicyRepository;
import com.example.library.domain.repository.FineRecordRepository;
import com.example.library.domain.repository.FineRuleRepository;
import com.example.library.domain.repository.LibraryItemRepository;
import com.example.library.domain.repository.LoanRepository;
import com.example.library.domain.repository.ReaderRepository;
import com.example.library.domain.repository.ReservationRepository;
import com.example.library.exception.BusinessException;
import com.example.library.exception.CardInvalidException;
import com.example.library.exception.ItemNotAvailableException;
import com.example.library.exception.ItemNotLoanableException;
import com.example.library.exception.ItemNotFoundException;
import com.example.library.exception.ItemNotOnLoanException;
import com.example.library.exception.ItemReservedByOtherException;
import com.example.library.exception.OverdueLoanExistsException;
import com.example.library.exception.QuotaExceededException;
import com.example.library.exception.ReaderNotFoundException;
import com.example.library.exception.UnpaidFineExistsException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 流通领域服务：实现借书（UC-101）与还书（UC-102）的校验链与状态变更。
 *
 * <p>业务规则集中在本服务与 BorrowPolicy / FineRule 中，Controller 不参与规则判断。
 * 事务边界由应用服务 {@code CirculationAppService} 定义。</p>
 */
@Service
public class CirculationService {

    private static final int RESERVATION_HOLD_DAYS = 7;

    private final BorrowCardRepository borrowCardRepository;
    private final ReaderRepository readerRepository;
    private final BorrowPolicyRepository borrowPolicyRepository;
    private final LoanRepository loanRepository;
    private final LibraryItemRepository libraryItemRepository;
    private final ReservationRepository reservationRepository;
    private final FineRuleRepository fineRuleRepository;
    private final FineRecordRepository fineRecordRepository;
    private final FineCalculator fineCalculator;

    public CirculationService(BorrowCardRepository borrowCardRepository,
                              ReaderRepository readerRepository,
                              BorrowPolicyRepository borrowPolicyRepository,
                              LoanRepository loanRepository,
                              LibraryItemRepository libraryItemRepository,
                              ReservationRepository reservationRepository,
                              FineRuleRepository fineRuleRepository,
                              FineRecordRepository fineRecordRepository,
                              FineCalculator fineCalculator) {
        this.borrowCardRepository = borrowCardRepository;
        this.readerRepository = readerRepository;
        this.borrowPolicyRepository = borrowPolicyRepository;
        this.loanRepository = loanRepository;
        this.libraryItemRepository = libraryItemRepository;
        this.reservationRepository = reservationRepository;
        this.fineRuleRepository = fineRuleRepository;
        this.fineRecordRepository = fineRecordRepository;
        this.fineCalculator = fineCalculator;
    }

    /**
     * 办理借书（UC-101）。
     *
     * <p>校验链（顺序固定，任一步失败即终止）：
     * 借阅证有效 → 无未缴罚款 → 无超期未还 → 未超借阅额度 → 副本可借且可外借 → 未被他人预约。</p>
     */
    public Loan borrowBook(String cardNo, String barcode, Long librarianId, LocalDate today) {
        BorrowCard card = borrowCardRepository.findByCardNo(cardNo)
                .filter(BorrowCard::isValid)
                .orElseThrow(() -> new CardInvalidException("借阅证 " + cardNo + " 不存在或已失效"));
        Long readerId = card.getReaderId();

        if (fineRecordRepository.existsUnpaidByReaderId(readerId)) {
            throw new UnpaidFineExistsException("读者存在未缴罚款，不能借书");
        }
        if (loanRepository.existsOverdueByReaderId(readerId, today)) {
            throw new OverdueLoanExistsException("读者存在超期未还图书，不能借书");
        }

        Reader reader = readerRepository.findById(readerId).orElseThrow(ReaderNotFoundException::new);
        BorrowPolicy policy = borrowPolicyRepository.findByReaderType(reader.getReaderType())
                .orElseThrow(() -> new BusinessException("POLICY_NOT_FOUND", "缺少该读者类型的借阅规则"));
        int currentLoanCount = (int) loanRepository.countActiveByReaderId(readerId);
        if (policy.exceedQuota(currentLoanCount)) {
            throw new QuotaExceededException(policy.getReaderType().displayName()
                    + "最多可借 " + policy.getMaxLoanCount() + " 本，当前已借 " + currentLoanCount + " 本");
        }

        LibraryItem item = libraryItemRepository.findByBarcode(barcode)
                .orElseThrow(() -> new ItemNotFoundException("条码 " + barcode + " 不是本馆藏书"));
        if (!item.isLoanable()) {
            throw new ItemNotLoanableException("该馆藏类型不可外借");
        }
        if (!item.isAvailable()) {
            throw new ItemNotAvailableException("副本当前状态不可借：" + item.getStatus().name());
        }

        Optional<Reservation> headReservation =
                reservationRepository.findHeadWaitingByTitleId(item.getBookTitle().getId());
        if (headReservation.isPresent() && !headReservation.get().getReaderId().equals(readerId)) {
            throw new ItemReservedByOtherException("该题名已被其他读者预约，仅队首读者可借");
        }

        LocalDate dueDate = policy.calcDueDate(today);
        Loan loan = loanRepository.save(new Loan(card.getId(), readerId, item.getId(), librarianId, today, dueDate));

        item.markOnLoan();
        libraryItemRepository.save(item);

        headReservation.filter(reservation -> reservation.getReaderId().equals(readerId))
                .ifPresent(reservation -> {
                    reservation.fulfill();
                    reservationRepository.save(reservation);
                });

        return loan;
    }

    /**
     * 办理还书（UC-102）：验证本馆藏书与在借状态 → 登记归还 → 计算超期罚款 → 恢复副本状态。
     */
    public ReturnResult returnBook(String barcode, Long librarianId, LocalDate today) {
        LibraryItem item = libraryItemRepository.findByBarcode(barcode)
                .orElseThrow(() -> new ItemNotFoundException("条码 " + barcode + " 不是本馆藏书"));
        if (!item.isOnLoan()) {
            throw new ItemNotOnLoanException("副本当前不在借出状态：" + item.getStatus().name());
        }
        Loan loan = loanRepository.findActiveByItemId(item.getId())
                .orElseThrow(() -> new ItemNotOnLoanException("未找到该副本的未归还借阅记录"));

        // 先在借出状态下计算超期天数，再登记归还（否则状态变为 RETURNED 后超期天数为 0）
        int overdueDays = loan.overdueDays(today);
        loan.markReturned(today);
        Money fineAmount = Money.zero();
        String fineStatus = "NONE";

        if (overdueDays > 0) {
            FineRule fineRule = fineRuleRepository.findByItemType(item.getItemType())
                    .orElseThrow(() -> new BusinessException("FINE_RULE_NOT_FOUND", "缺少该借出物类型的罚款规则"));
            Optional<FineRecord> fineRecord = fineCalculator.calculate(loan, fineRule, overdueDays);
            if (fineRecord.isPresent()) {
                FineRecord saved = fineRecordRepository.save(fineRecord.get());
                fineAmount = saved.getAmount();
                fineStatus = saved.getStatus().name();
            }
        }

        loanRepository.save(loan);

        Optional<Reservation> headReservation =
                reservationRepository.findHeadWaitingByTitleId(item.getBookTitle().getId());
        if (headReservation.isPresent()) {
            item.markReserved();
            Reservation reservation = headReservation.get();
            reservation.holdUntil(today.plusDays(RESERVATION_HOLD_DAYS).atStartOfDay());
            reservationRepository.save(reservation);
        } else {
            item.markAvailable();
        }
        libraryItemRepository.save(item);

        return new ReturnResult(loan.getId(), today, overdueDays, fineAmount, fineStatus);
    }
}
