package com.example.library.application;

import com.example.library.exception.PermissionDeniedException;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 权限校验器：集中实现三类角色的访问控制（07-architecture.md 第 4 节）。
 */
@Component
public class PermissionChecker {

    /** 借还操作只能由图书管理员执行（BR-013）。 */
    public void assertLibrarian(Operator operator) {
        if (operator == null || operator.role() != OperatorRole.LIBRARIAN) {
            throw new PermissionDeniedException("只有图书管理员可以执行借还操作");
        }
    }

    /** 系统维护操作只能由系统管理员执行（BR-014）。 */
    public void assertAdmin(Operator operator) {
        if (operator == null || operator.role() != OperatorRole.SYSTEM_ADMIN) {
            throw new PermissionDeniedException("只有系统管理员可以执行维护操作");
        }
    }

    /** 读者只能访问本人数据，图书管理员与系统管理员可代查（BR-015）。 */
    public void assertSelfOrStaff(Operator operator, Long readerId) {
        if (operator == null) {
            throw new PermissionDeniedException("缺少操作人信息");
        }
        if (operator.role() == OperatorRole.READER && !Objects.equals(operator.id(), readerId)) {
            throw new PermissionDeniedException("读者只能访问本人的借阅信息");
        }
    }

    /** 图书管理员或系统管理员。 */
    public void assertStaff(Operator operator) {
        if (operator == null
                || (operator.role() != OperatorRole.LIBRARIAN && operator.role() != OperatorRole.SYSTEM_ADMIN)) {
            throw new PermissionDeniedException("只有图书管理员或系统管理员可以执行该操作");
        }
    }
}
