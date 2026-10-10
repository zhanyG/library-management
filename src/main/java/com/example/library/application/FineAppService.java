package com.example.library.application;

import com.example.library.domain.model.fine.FineRecord;
import com.example.library.domain.repository.FineRecordRepository;
import com.example.library.exception.BusinessException;
import com.example.library.presentation.dto.FineResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 罚款应用服务：查询与缴纳（FR-015）。
 */
@Service
public class FineAppService {

    private final FineRecordRepository fineRecordRepository;
    private final PermissionChecker permissionChecker;

    public FineAppService(FineRecordRepository fineRecordRepository, PermissionChecker permissionChecker) {
        this.fineRecordRepository = fineRecordRepository;
        this.permissionChecker = permissionChecker;
    }

    /** 查询罚款：读者仅本人，图书管理员/系统管理员可代查（BR-015）。 */
    @Transactional(readOnly = true)
    public List<FineResponse> listFines(Long readerId, Operator operator) {
        permissionChecker.assertSelfOrStaff(operator, readerId);
        return fineRecordRepository.findByReaderId(readerId).stream().map(this::toResponse).toList();
    }

    /** 缴纳罚款：仅图书管理员或系统管理员可登记。 */
    @Transactional
    public FineResponse pay(Long fineId, Operator operator) {
        permissionChecker.assertStaff(operator);
        FineRecord fineRecord = fineRecordRepository.findById(fineId)
                .orElseThrow(() -> new BusinessException("FINE_NOT_FOUND", "罚款记录 " + fineId + " 不存在"));
        fineRecord.pay();
        return toResponse(fineRecordRepository.save(fineRecord));
    }

    private FineResponse toResponse(FineRecord fineRecord) {
        return new FineResponse(fineRecord.getId(), fineRecord.getLoanId(), fineRecord.getReaderId(),
                fineRecord.getAmount().amount(), fineRecord.getAmount().currency(),
                fineRecord.getStatus().name(), fineRecord.getCreatedAt());
    }
}
