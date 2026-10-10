package com.example.library.infrastructure.persistence;

import com.example.library.domain.model.circulation.BorrowPolicy;
import com.example.library.domain.model.reader.ReaderType;
import com.example.library.domain.repository.BorrowPolicyRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 借阅规则初始化器：按 specs/01-clarifying-questions.md Q10/Q11 写入各读者类型的借阅数量与期限。
 *
 * <p>仅在规则表为空时写入，避免覆盖系统管理员维护后的规则。</p>
 */
@Component
@Order(1)
public class BorrowPolicyInitializer implements ApplicationRunner {

    private final BorrowPolicyRepository borrowPolicyRepository;

    public BorrowPolicyInitializer(BorrowPolicyRepository borrowPolicyRepository) {
        this.borrowPolicyRepository = borrowPolicyRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!borrowPolicyRepository.findAll().isEmpty()) {
            return;
        }
        List<BorrowPolicy> defaults = List.of(
                new BorrowPolicy(ReaderType.JUNIOR_COLLEGE, 5, 30),
                new BorrowPolicy(ReaderType.UNDERGRADUATE, 5, 30),
                new BorrowPolicy(ReaderType.POSTGRADUATE, 10, 60),
                new BorrowPolicy(ReaderType.DOCTORAL, 15, 60),
                new BorrowPolicy(ReaderType.TEACHER, 20, 90));
        defaults.forEach(borrowPolicyRepository::save);
    }
}
