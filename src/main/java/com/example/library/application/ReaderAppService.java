package com.example.library.application;

import com.example.library.domain.model.reader.Reader;
import com.example.library.domain.model.reader.ReaderType;
import com.example.library.domain.model.reader.StudentReader;
import com.example.library.domain.model.reader.TeacherReader;
import com.example.library.domain.repository.LoanRepository;
import com.example.library.domain.repository.ReaderRepository;
import com.example.library.exception.DuplicateIdCardException;
import com.example.library.exception.ReaderNotFoundException;
import com.example.library.presentation.dto.LoanQueryResponse;
import com.example.library.presentation.dto.ReaderResponse;
import com.example.library.presentation.dto.RegisterReaderRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 读者应用服务：注册读者（FR-001）与查询借阅信息（FR-013）。
 */
@Service
public class ReaderAppService {

    private final ReaderRepository readerRepository;
    private final LoanRepository loanRepository;
    private final LoanViewAssembler loanViewAssembler;
    private final PermissionChecker permissionChecker;

    public ReaderAppService(ReaderRepository readerRepository,
                            LoanRepository loanRepository,
                            LoanViewAssembler loanViewAssembler,
                            PermissionChecker permissionChecker) {
        this.readerRepository = readerRepository;
        this.loanRepository = loanRepository;
        this.loanViewAssembler = loanViewAssembler;
        this.permissionChecker = permissionChecker;
    }

    /** FR-001 注册读者账号。 */
    @Transactional
    public ReaderResponse register(RegisterReaderRequest request) {
        if (readerRepository.existsByIdCardNo(request.idCardNo())) {
            throw new DuplicateIdCardException("证件号 " + request.idCardNo() + " 已注册");
        }
        Reader reader = toReader(request);
        Reader saved = readerRepository.save(reader);
        return new ReaderResponse(saved.getId(), saved.getName(), saved.getDepartment(),
                saved.getReaderType().name(), saved.getStatus().name());
    }

    /** FR-013 查询借阅信息：读者仅本人，图书管理员/系统管理员可代查（BR-015）。 */
    @Transactional(readOnly = true)
    public List<LoanQueryResponse> listLoans(Long readerId, Operator operator, LocalDate today) {
        permissionChecker.assertSelfOrStaff(operator, readerId);
        readerRepository.findById(readerId).orElseThrow(ReaderNotFoundException::new);
        return loanRepository.findByReaderId(readerId).stream()
                .map(loan -> loanViewAssembler.toQueryResponse(loan, today))
                .toList();
    }

    private Reader toReader(RegisterReaderRequest request) {
        if (request.readerType() == ReaderType.TEACHER) {
            return new TeacherReader(request.name(), request.idCardNo(), request.phone(), request.department(),
                    request.employeeNo(), request.title(), request.faculty());
        }
        return new StudentReader(request.name(), request.idCardNo(), request.phone(), request.department(),
                request.readerType(), request.studentNo(), request.grade(), request.major());
    }
}
