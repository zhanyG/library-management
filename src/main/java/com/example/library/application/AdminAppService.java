package com.example.library.application;

import com.example.library.domain.model.catalog.ItemType;
import com.example.library.domain.model.circulation.BorrowPolicy;
import com.example.library.domain.model.fine.FineRule;
import com.example.library.domain.model.fine.Money;
import com.example.library.domain.model.reader.Librarian;
import com.example.library.domain.model.reader.ReaderType;
import com.example.library.domain.repository.BorrowPolicyRepository;
import com.example.library.domain.repository.FineRuleRepository;
import com.example.library.domain.repository.LibrarianRepository;
import com.example.library.exception.BusinessException;
import com.example.library.exception.LibrarianNotFoundException;
import com.example.library.presentation.dto.AddLibrarianRequest;
import com.example.library.presentation.dto.UpdateBorrowPolicyRequest;
import com.example.library.presentation.dto.UpdateFineRuleRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 管理端应用服务：管理员账号与规则维护（FR-004、FR-005、FR-016，仅系统管理员 BR-014）。
 */
@Service
public class AdminAppService {

    private final LibrarianRepository librarianRepository;
    private final BorrowPolicyRepository borrowPolicyRepository;
    private final FineRuleRepository fineRuleRepository;
    private final PermissionChecker permissionChecker;

    public AdminAppService(LibrarianRepository librarianRepository,
                           BorrowPolicyRepository borrowPolicyRepository,
                           FineRuleRepository fineRuleRepository,
                           PermissionChecker permissionChecker) {
        this.librarianRepository = librarianRepository;
        this.borrowPolicyRepository = borrowPolicyRepository;
        this.fineRuleRepository = fineRuleRepository;
        this.permissionChecker = permissionChecker;
    }

    /** FR-004 添加图书管理员。 */
    @Transactional
    public void addLibrarian(AddLibrarianRequest request, Operator operator) {
        permissionChecker.assertAdmin(operator);
        if (librarianRepository.existsByStaffNo(request.staffNo())) {
            throw new BusinessException("LIBRARIAN_ALREADY_EXISTS", "工号 " + request.staffNo() + " 已存在");
        }
        librarianRepository.save(new Librarian(request.staffNo(), request.name()));
    }

    /** FR-005 删除（停用）图书管理员。 */
    @Transactional
    public void removeLibrarian(String staffNo, Operator operator) {
        permissionChecker.assertAdmin(operator);
        Librarian librarian = librarianRepository.findByStaffNo(staffNo)
                .orElseThrow(() -> new LibrarianNotFoundException("工号 " + staffNo + " 不存在"));
        librarian.disable();
        librarianRepository.save(librarian);
    }

    /** FR-016 维护借阅规则，更新后立即对新的借书生效。 */
    @Transactional
    public void updateBorrowPolicy(ReaderType readerType, UpdateBorrowPolicyRequest request, Operator operator) {
        permissionChecker.assertAdmin(operator);
        BorrowPolicy policy = borrowPolicyRepository.findByReaderType(readerType)
                .orElseThrow(() -> new BusinessException("POLICY_NOT_FOUND", "读者类型 " + readerType + " 缺少规则"));
        policy.update(request.maxLoanCount(), request.loanPeriodDays());
        borrowPolicyRepository.save(policy);
    }

    /** FR-016 维护罚款规则，更新后立即对新的还书生效。 */
    @Transactional
    public void updateFineRule(ItemType itemType, UpdateFineRuleRequest request, Operator operator) {
        permissionChecker.assertAdmin(operator);
        FineRule fineRule = fineRuleRepository.findByItemType(itemType)
                .orElseThrow(() -> new BusinessException("FINE_RULE_NOT_FOUND", "借出物类型 " + itemType + " 缺少规则"));
        fineRule.update(request.gracePeriodDays(), Money.of(request.dailyFine()), Money.of(request.maxFine()));
        fineRuleRepository.save(fineRule);
    }
}
