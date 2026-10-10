# 详细设计模型（Detailed Design Model）

> 输入规格：`02-requirements.md`、`03-use-cases.md`、`05-domain-model.md`、`07-architecture.md`、`08-package-diagram.puml`、`13-database-design.md`、`constitution.md`
> 本文档描述实验二的详细设计：以用例为单元，给出实现类、接口、DTO、异常与事务边界。

## 1. 分层到包的映射

| 层 | Java 包 | 主要类 |
|---|---|---|
| Presentation / Controller | `com.example.library.presentation.controller` | `ReaderController`、`CardController`、`CatalogController`、`CirculationController`、`ReservationController`、`FineController`、`AdminController` |
| Presentation / DTO | `com.example.library.presentation.dto` | `RegisterReaderRequest`、`IssueCardRequest`、`BorrowBookRequest`、`ReturnBookRequest`、`ReserveBookRequest`、`CreateTitleRequest`、`AddItemRequest`、`UpdateBorrowPolicyRequest`、`UpdateFineRuleRequest`、`LoanResponse`、`CardResponse`、`ReservationResponse`、`FineResponse`、`ApiResponse`、`ErrorResponse` |
| Application | `com.example.library.application` | `ReaderAppService`、`CardAppService`、`CatalogAppService`、`CirculationAppService`、`ReservationAppService`、`FineAppService`、`AdminAppService`、`PermissionChecker` |
| Domain / Model | `com.example.library.domain.model.*` | `Reader`、`StudentReader`、`TeacherReader`、`BorrowCard`、`BookTitle`、`LibraryItem`、`Book`、`Magazine`、`Thesis`、`Loan`、`Reservation`、`BorrowPolicy`、`FineRule`、`FineRecord`、`Money`、枚举 |
| Domain / Service | `com.example.library.domain.service` | `CirculationService`、`ReservationService`、`FineCalculator`、`NumberGenerator` |
| Domain / Repository（端口） | `com.example.library.domain.repository` | `ReaderRepository`、`BorrowCardRepository`、`BookTitleRepository`、`LibraryItemRepository`、`LoanRepository`、`ReservationRepository`、`BorrowPolicyRepository`、`FineRuleRepository`、`FineRecordRepository` |
| Infrastructure | `com.example.library.infrastructure.persistence` | `Jpa*EntityRepository`（Spring Data 接口）、`*RepositoryAdapter`（实现端口）、`SequenceGeneratorAdapter` |
| Exception | `com.example.library.exception` | `BusinessException` 及各子类、`GlobalExceptionHandler` |
| Bootstrap | `com.example.library` | `LibraryApplication` |

> 命名对齐说明：实验一包图中的 `CirculationController` 在本设计中承担“图书管理员代理借还”职责，对应实验二指导文档中的 `LibrarianController`；`FineCalculator` 对应指导文档中的 `FineService`，`FineRuleRepository` 对应 `FineRuleService` 的规则查询职责。名称差异仅为细化，不改变分层语义。

## 2. 通用设计约定

### 2.1 权限约定

- 操作人由请求携带 `operatorId` + `operatorRole`（`READER` / `LIBRARIAN` / `SYSTEM_ADMIN`），由 `PermissionChecker` 统一校验。
- 应用服务入口第一行执行权限断言（`assertLibrarian()`、`assertAdmin()`、`assertSelf(readerId)`），越权抛 `PermissionDeniedException`（`PERMISSION_DENIED`）。

### 2.2 异常约定

- 所有业务异常继承 `BusinessException`，携带 `code` 与 `message`。
- `GlobalExceptionHandler`（`@RestControllerAdvice`）统一转换为 `ErrorResponse{code,message,traceId}`。
- 错误码取值与 `07-architecture.md` 第 5 节异常表一致：`CARD_INVALID`、`QUOTA_EXCEEDED`、`READER_HAS_OVERDUE_LOAN`、`READER_HAS_UNPAID_FINE`、`ITEM_NOT_AVAILABLE`、`ITEM_RESERVED_BY_OTHER`、`ITEM_NOT_LOANABLE`、`ITEM_NOT_ON_LOAN`、`ITEM_NOT_FOUND`、`PERMISSION_DENIED`、`DUPLICATE_RESERVATION`、`RESERVATION_LIMIT_EXCEEDED`、`INVALID_POLICY_VALUE`、`CARD_ALREADY_EXISTS`、`CARD_HAS_ACTIVE_LOANS`、`TITLE_ALREADY_EXISTS`、`TITLE_HAS_ACTIVE_LOANS`、`READER_NOT_FOUND`、`TITLE_NOT_FOUND`、`LIBRARIAN_NOT_FOUND`。

### 2.3 事务边界

- 事务注解只出现在**应用服务**方法上。
- 借书、还书、预约的多次状态变更在单个事务内完成（NFR-004）。
- 领域服务与实体保持纯内存状态变更，不开启事务。

### 2.4 Repository 端口与适配器

- 领域层定义端口接口（如 `LoanRepository`），仅包含领域语义方法：`save`、`findActiveByItemId`、`countActiveByReaderId`、`existsOverdueByReaderId` 等。
- 基础设施层用 Spring Data JPA 接口（`JpaLoanRepository`）+ 适配器（`LoanRepositoryAdapter`）实现端口，完成实体 ↔ 持久化映射。

### 2.5 实现折中（须人工审查）

| 折中 | 原因 | 影响 |
|---|---|---|
| 领域实体使用 JPA 注解（`@Entity`/`@Id`） | 避免双模型映射成本，教学规模下收益更高 | 领域层引入 `jakarta.persistence` 依赖，非 100% 框架无关；业务规则仍全部位于领域方法与策略类中 |
| 继承采用单表策略（`@Inheritance(SINGLE_TABLE)`） | 与 `13-database-design.md` 的映射策略一致 | 子类字段可空 |
| 权限不做真实认证 | 与 `01-clarifying-questions.md` Q01 人类回答一致 | 依赖调用方传入 operatorRole，测试覆盖越权场景 |

## 3. 用例详细设计

### 3.1 UC-101 办理借书

| 设计项 | 内容 |
|---|---|
| 界面入口 | `CirculationController.borrowBook(@Valid BorrowBookRequest)` → `POST /api/circulation/borrow` |
| Application Service | `CirculationAppService.borrowBook(BorrowBookCommand)`：`@Transactional`；权限校验（LIBRARIAN）→ 调领域服务 → 组装 `LoanResponse` |
| Domain Service | `CirculationService.borrowBook(cardNo, barcode, librarianId, today)`：借阅证校验 → 未缴罚款校验 → 超期未还校验 → 额度校验 → 副本可借与预约占用校验 → 创建 `Loan` → 更新 `LibraryItem` 状态 |
| Entity / Aggregate | `BorrowCard`（有效性）、`Reader`（在借数量、超期、罚款）、`Loan`（新建）、`LibraryItem`（状态 AVAILABLE→ON_LOAN）、`BorrowPolicy`（额度与期限）、`Reservation`（队首预约置 FULFILLED） |
| Repository | `BorrowCardRepository`、`ReaderRepository`、`LoanRepository`、`LibraryItemRepository`、`BorrowPolicyRepository`、`FineRecordRepository`、`ReservationRepository` |
| DTO | 入参 `BorrowBookRequest{cardNo, barcode, operatorId, operatorRole}`；出参 `LoanResponse{loanId, readerName, titleName, barcode, borrowDate, dueDate, status}` |
| 异常 | `CardInvalidException`(`CARD_INVALID`)、`UnpaidFineExistsException`(`READER_HAS_UNPAID_FINE`)、`OverdueLoanExistsException`(`READER_HAS_OVERDUE_LOAN`)、`QuotaExceededException`(`QUOTA_EXCEEDED`)、`ItemNotAvailableException`(`ITEM_NOT_AVAILABLE`)、`ItemReservedByOtherException`(`ITEM_RESERVED_BY_OTHER`)、`ItemNotLoanableException`(`ITEM_NOT_LOANABLE`)、`PermissionDeniedException`(`PERMISSION_DENIED`) |
| 事务边界 | `CirculationAppService.borrowBook` 整体一个事务：创建 Loan + 更新 Item + 更新 Reservation 要么全成功要么全回滚 |

**校验顺序（与 UC-101 基本事件流一致，任一步失败立即终止）：**

1. 权限：operatorRole = LIBRARIAN
2. `BorrowCard` 存在且 `isValid()`
3. 读者无未缴罚款
4. 读者无超期未还 `Loan`
5. 在借数量 < `BorrowPolicy.maxLoanCount`
6. `LibraryItem` 存在且 `isAvailable()` 且 `loanable`
7. 该题名若存在 `WAITING` 预约，仅队首读者可借
8. 创建 `Loan`，`dueDate = borrowDate + BorrowPolicy.loanPeriodDays`
9. 副本状态 → `ON_LOAN`；若借阅人为队首预约者，预约 → `FULFILLED`

### 3.2 UC-102 办理还书

| 设计项 | 内容 |
|---|---|
| 界面入口 | `CirculationController.returnBook(@Valid ReturnBookRequest)` → `POST /api/circulation/return` |
| Application Service | `CirculationAppService.returnBook(ReturnBookCommand)`：`@Transactional`；权限校验（LIBRARIAN）→ 领域服务 → `ReturnBookResponse` |
| Domain Service | `CirculationService.returnBook(barcode, librarianId, today)`：验本馆藏书 → 查未还 `Loan` → 登记归还 → 超期判定与罚款计算 → 关闭 Loan → 副本状态恢复 → 队首预约置 RESERVED |
| Entity / Aggregate | `LibraryItem`（ON_LOAN→AVAILABLE/RESERVED）、`Loan`（BORROWED/OVERDUE→RETURNED）、`FineRule`、`FineRecord`（超期新建）、`Reservation`（队首保留） |
| Repository | `LibraryItemRepository`、`LoanRepository`、`FineRuleRepository`、`FineRecordRepository`、`ReservationRepository` |
| DTO | 入参 `ReturnBookRequest{barcode, operatorId, operatorRole}`；出参 `ReturnBookResponse{loanId, returnDate, overdueDays, fineAmount, fineStatus}` |
| 异常 | `ItemNotFoundException`(`ITEM_NOT_FOUND`)、`ItemNotOnLoanException`(`ITEM_NOT_ON_LOAN`)、`PermissionDeniedException` |
| 事务边界 | 登记归还 + 生成罚款 + 更新副本 + 处理预约在同一事务 |
| 罚款计算 | 领域服务 `FineCalculator.calculate(loan, fineRule, returnDate)`：`overdueDays = max(0, returnDate - dueDate - gracePeriodDays)`；`amount = min(overdueDays × dailyFine, maxFine)`；`overdueDays = 0` 时不生成 `FineRecord` |

### 3.3 UC-004 预约图书

| 设计项 | 内容 |
|---|---|
| 界面入口 | `ReservationController.reserve(@Valid ReserveBookRequest)` → `POST /api/reservations`；`cancel` → `DELETE /api/reservations/{id}` |
| Application Service | `ReservationAppService.reserve(...)`：`@Transactional`；权限（READER 且只能为本人预约）→ 领域服务 → `ReservationResponse` |
| Domain Service | `ReservationService.reserve(readerId, titleId, now)`：读者/题名存在校验 → 重复预约校验 → 预约上限（3）校验 → 计算 `queuePosition`（同题名 WAITING 数 + 1）→ 创建 `Reservation` |
| Entity / Aggregate | `Reader`、`BookTitle`、`Reservation`（新建，状态 WAITING） |
| Repository | `ReaderRepository`、`BookTitleRepository`、`ReservationRepository` |
| DTO | 入参 `ReserveBookRequest{readerId, titleId, operatorId, operatorRole}`；出参 `ReservationResponse{reservationId, titleName, queuePosition, createdAt, status}` |
| 异常 | `ReaderNotFoundException`、`TitleNotFoundException`、`DuplicateReservationException`(`DUPLICATE_RESERVATION`)、`ReservationLimitExceededException`(`RESERVATION_LIMIT_EXCEEDED`)、`PermissionDeniedException` |
| 事务边界 | 校验 + 排队序号分配 + 创建预约在同一事务（避免并发下序号冲突） |

### 3.4 UC-003 查询借阅信息

| 设计项 | 内容 |
|---|---|
| 界面入口 | `ReaderController.myLoans(readerId, operatorId, operatorRole)` → `GET /api/readers/{readerId}/loans`；`CirculationController.readerLoans(cardNo, ...)` → `GET /api/circulation/readers/{cardNo}/loans` |
| Application Service | `ReaderAppService.listLoans(readerId, operator)`：权限（本人或 LIBRARIAN 或 SYSTEM_ADMIN）；`CirculationAppService.listLoansByCard(cardNo, operator)`：权限（LIBRARIAN） |
| Domain Service | 无（纯查询，规则为“超期判定”落在 `Loan.isOverdue(today)`） |
| Entity / Aggregate | `Loan`、`Reader`、`LibraryItem`、`BookTitle` |
| Repository | `LoanRepository.findByReaderId`、`BorrowCardRepository.findByCardNo`、`LibraryItemRepository` |
| DTO | `LoanQueryResponse{loanId, titleName, barcode, borrowDate, dueDate, returnDate, status, overdue, overdueDays}` |
| 异常 | `PermissionDeniedException`、`ReaderNotFoundException`、`CardInvalidException` |
| 事务边界 | 只读，`@Transactional(readOnly = true)` |

### 3.5 UC-201 办理借阅证

| 设计项 | 内容 |
|---|---|
| 界面入口 | `CardController.issue(@Valid IssueCardRequest)` → `POST /api/admin/borrow-cards`；`revoke` → `DELETE /api/admin/borrow-cards/{cardNo}` |
| Application Service | `CardAppService.issueCard(readerId, operator)`、`revokeCard(cardNo, operator)`；`@Transactional`；权限（SYSTEM_ADMIN） |
| Domain Service | `NumberGenerator.nextCardNo()`（`CARD+年份+6位序号`）；唯一性校验通过 `BorrowCardRepository.existsByCardNo` |
| Entity / Aggregate | `Reader`（存在性）、`BorrowCard`（新建，状态 VALID） |
| Repository | `ReaderRepository`、`BorrowCardRepository`、`LoanRepository` |
| DTO | `IssueCardRequest{readerId, operatorId, operatorRole}`；`CardResponse{cardId, cardNo, ownerName, department, issueDate, status}` |
| 异常 | `ReaderNotFoundException`、`CardAlreadyExistsException`(`CARD_ALREADY_EXISTS`)、`CardHasActiveLoansException`(`CARD_HAS_ACTIVE_LOANS`)、`PermissionDeniedException` |
| 事务边界 | 校验 + 编号生成 + 保存同一事务；注销时先检查未还借阅 |

### 3.6 UC-205 添加图书标题信息

| 设计项 | 内容 |
|---|---|
| 界面入口 | `CatalogController.createTitle(@Valid CreateTitleRequest)` → `POST /api/admin/book-titles`；删除 → `DELETE /api/admin/book-titles/{id}` |
| Application Service | `CatalogAppService.createTitle(command, operator)`；权限（SYSTEM_ADMIN） |
| Domain Service | ISBN 唯一性校验（`BookTitleRepository.existsByIsbn`）；`ItemType` 决定默认 `loanable`（THESIS → false） |
| Entity / Aggregate | `BookTitle`（新建） |
| Repository | `BookTitleRepository`、`LibraryItemRepository`（删除前的在借检查） |
| DTO | `CreateTitleRequest{name, author, isbn, publisher, publishedYear, itemType, operatorId, operatorRole}`；`TitleResponse{titleId, name, isbn, itemType, availableCount}` |
| 异常 | `TitleAlreadyExistsException`(`TITLE_ALREADY_EXISTS`)、`TitleHasActiveLoansException`(`TITLE_HAS_ACTIVE_LOANS`)、`PermissionDeniedException` |
| 事务边界 | 单表写入，事务包裹校验 + 保存 |

### 3.7 UC-207 添加馆藏副本

| 设计项 | 内容 |
|---|---|
| 界面入口 | `CatalogController.addItem(@Valid AddItemRequest)` → `POST /api/admin/library-items`；删除 → `DELETE /api/admin/library-items/{barcode}` |
| Application Service | `CatalogAppService.addItem(command, operator)`；权限（SYSTEM_ADMIN） |
| Domain Service | `NumberGenerator.nextBarcode()`（`ITEM+年份+6位序号`）；条码唯一性校验 |
| Entity / Aggregate | `BookTitle`（存在性）、`LibraryItem`（新建，状态 AVAILABLE，按题名类型实例化 Book/Magazine/Thesis） |
| Repository | `BookTitleRepository`、`LibraryItemRepository` |
| DTO | `AddItemRequest{titleId, location, operatorId, operatorRole}`；`ItemResponse{barcode, titleName, itemType, status}` |
| 异常 | `TitleNotFoundException`(`TITLE_NOT_FOUND`)、`DuplicateBarcodeException`、`ItemOnLoanCannotDeleteException`(`ITEM_ON_LOAN_CANNOT_DELETE`)、`PermissionDeniedException` |
| 事务边界 | 校验 + 生成条码 + 保存同一事务 |

### 3.8 UC-210 计算超期罚款

| 设计项 | 内容 |
|---|---|
| 界面入口 | `FineController.listFines` → `GET /api/fines?readerId=`；`pay` → `POST /api/fines/{id}/pay`（图书管理员/系统管理员） |
| Application Service | `FineAppService.listUnpaid(readerId, operator)`、`pay(fineId, operator)` |
| Domain Service | `FineCalculator.calculate(loan, fineRule, returnDate)`：`OverdueDays` 计算 → 费率查表 → 上限截断 → 生成 `FineRecord` |
| Entity / Aggregate | `Loan`（超期依据）、`FineRule`（免罚期/每日金额/上限）、`FineRecord`（新建，状态 UNPAID/PAID） |
| Repository | `FineRuleRepository`、`FineRecordRepository`、`LoanRepository` |
| DTO | `FineResponse{fineId, loanId, readerName, amount, currency, status, createdAt}` |
| 异常 | `InvalidPolicyValueException`(`INVALID_POLICY_VALUE`)、`PermissionDeniedException` |
| 事务边界 | 罚款生成在还书事务内；缴纳单独事务 |
| 规则取值 | CHINESE_BOOK 3/0.20/50、FOREIGN_BOOK 5/0.50/100、CHINESE_MAGAZINE 1/0.50/20、FOREIGN_MAGAZINE 2/1.00/50、THESIS 0/2.00/100（与 `01-clarifying-questions.md` Q19 一致） |

### 3.9 UC-209 维护借阅规则和罚款规则

| 设计项 | 内容 |
|---|---|
| 界面入口 | `AdminController.updateBorrowPolicy` → `PUT /api/admin/borrow-policies/{readerType}`；`updateFineRule` → `PUT /api/admin/fine-rules/{itemType}` |
| Application Service | `AdminAppService.updateBorrowPolicy(...)`、`updateFineRule(...)`；权限（SYSTEM_ADMIN） |
| Entity / Aggregate | `BorrowPolicy`（maxLoanCount、loanPeriodDays）、`FineRule`（gracePeriodDays、dailyFine、maxFine） |
| 异常 | `InvalidPolicyValueException`（负数/空值/上限 < 0）、`PermissionDeniedException` |
| 事务边界 | 单条规则更新，事务包裹 |

## 4. 领域层类职责清单

| 类 | 职责 | 关键方法 |
|---|---|---|
| `Reader`(abstract) / `StudentReader` / `TeacherReader` | 读者身份与类型 | `isActive()`、`readerType()` |
| `BorrowCard` | 借阅凭证有效性 | `isValid()`、`revoke()`、`markLost()` |
| `BookTitle` | 题名元数据与可借副本统计 | `isLoanable()`、`addItem()` |
| `LibraryItem`(abstract) / `Book` / `Magazine` / `Thesis` | 副本状态机 | `isAvailable()`、`markOnLoan()`、`markAvailable()`、`markReserved()`、`withdraw()` |
| `Loan` | 借阅生命周期与超期判定 | `isOverdue(today)`、`overdueDays(today)`、`markReturned(date)`、`markOverdue()` |
| `Reservation` | 预约排队状态 | `isHeadOfQueue()`、`cancel()`、`expire(now)`、`fulfill()` |
| `BorrowPolicy` | 读者类型借阅规则（Strategy） | `exceedQuota(count)`、`calcDueDate(from)` |
| `FineRule` | 借出物类型罚款规则（Strategy） | `calcFine(overdueDays)` |
| `FineRecord` | 罚款状态 | `pay()`、`isUnpaid()` |
| `Money` | 金额值对象 | `of()`、`plus()`、`times()`、`cappedAt()`、`isZero()` |
| `CirculationService` | 借书/还书流程编排与校验链（Template Method） | `borrowBook(...)`、`returnBook(...)` |
| `ReservationService` | 预约排队与到书处理 | `reserve(...)`、`cancel(...)`、`nextWaiting(...)` |
| `FineCalculator` | 罚款计算 | `calculate(loan, rule, returnDate)` |

## 5. 设计模式落点

| 模式 | 落点 |
|---|---|
| Strategy | `BorrowPolicy`（读者类型规则）、`FineRule`（借出物类型规则），由 Repository 按类型键查表获得 |
| Factory | `LibraryItemFactory`（按 `ItemType` 实例化 Book/Magazine/Thesis）、`ReaderFactory`（`@Inheritance` + `ReaderType` 分支） |
| Repository（端口-适配器） | `domain.repository` 定义端口，`infrastructure.persistence.*RepositoryAdapter` 实现 |
| Service Layer | 7 个 `*AppService` 组织用例流程与事务 |
| Template Method | `CirculationService.borrowBook` 的固定校验链（步骤固定，规则来自 Policy/Repository） |
| DTO | `presentation.dto` 与领域对象隔离 |
| MVC | `*Controller`（C）+ JSON 响应（V）+ 领域模型（M） |
| Value Object | `Money` |

## 6. 与实验一 Specs 的一致性检查

| 检查项 | 结论 |
|---|---|
| Controller 不含业务规则 | 是，规则在 `CirculationService`、`BorrowPolicy`、`FineRule` |
| Controller 不直接依赖 Repository | 是，只依赖 `*AppService` |
| 事务边界在应用服务层 | 是 |
| 权限三类角色区分 | 是，`PermissionChecker` + 应用服务断言 |
| 业务规则集中在 Policy / 领域服务 | 是 |
| 数据库设计一致 | 表结构与 `13-database-design.md` 一致（单表继承、唯一约束、DECIMAL 金额） |

## 7. 人工审查记录

| 轮次 | 问题 | 修订 |
|---|---|---|
| 1 | 初稿未说明 Controller 命名与指导文档（LibrarianController / FineService）的对应关系 | 已在 §1 增加“命名对齐说明” |
| 2 | 初稿未明确“尚未缴罚款是否阻止借书”的检查位置 | 已在 §3.1 校验链第 3 步明确，并列入 `FineRecordRepository` 依赖 |
| 3 | 初稿未记录领域实体使用 JPA 注解的折中 | 已补充 §2.5 实现折中表 |
