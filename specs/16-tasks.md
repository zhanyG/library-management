# 实现任务拆解（Implementation Tasks）

> 使用方式：编码 Agent 一次只做一个任务；开始前输出实现计划；完成后运行该任务的建议测试。
> **全局约束：`specs/` 已建立 baseline，任何任务都不得修改 `specs/` 下任何文件。**

## 全局约定

| 项 | 约定 |
|---|---|
| 技术栈 | Java 17（`maven.compiler.release=17`）+ Spring Boot 3.5.x + Spring Web + Spring Data JPA + H2 + Validation + JUnit 5 |
| 构建 | Maven（`D:\Tools\apache-maven-3.9.15`），`mvn -q test` |
| 根包 | `com.example.library` |
| 允许修改目录 | `src/main/java/**`、`src/main/resources/**`、`src/test/java/**`、`src/test/resources/**`、`pom.xml`、`README.md` |
| 不允许修改 | `specs/**`、`tools/**`、`experiment1-run.log` |
| 完成定义 | 该任务新增/修改的代码可编译；对应测试通过；`git status` 无多余文件 |

---

## TASK-001 初始化项目结构

- 目标：建立可编译、可跑测试的 Maven + Spring Boot 骨架，创建分层包与启动类。
- 输入规格：`07-architecture.md`、`08-package-diagram.puml`、`16-tasks.md`（本文件）
- 允许修改：`pom.xml`、`src/main/java/com/example/library/LibraryApplication.java`、各分层包下的 `package-info.java` 或空包占位、`src/main/resources/application.yml`、`src/test/java/com/example/library/LibraryApplicationTests.java`
- 实现要求：
  1. `pom.xml` 引入 spring-boot-starter-web、spring-boot-starter-data-jpa、spring-boot-starter-validation、h2、spring-boot-starter-test；`maven.compiler.release=17`。
  2. 建立包：`presentation.controller`、`presentation.dto`、`application`、`domain.model.{reader,catalog,circulation,reservation,fine}`、`domain.service`、`domain.repository`、`infrastructure.persistence`、`exception`。
  3. `application.yml`：H2 内存库 `jdbc:h2:mem:library`、`ddl-auto=create-drop`、开启 H2 console。
  4. 不实现任何业务逻辑。
- 验收标准：`mvn -q test` 构建成功，`LibraryApplicationTests.contextLoads` 通过。
- 建议测试：`LibraryApplicationTests`（上下文加载）

## TASK-002 实现领域实体与枚举

- 目标：按领域模型与数据库设计实现全部实体、值对象与枚举。
- 输入规格：`05-domain-model.md`、`13-database-design.md`、`09-design-model.md` §1
- 允许修改：`src/main/java/com/example/library/domain/model/**`、`src/test/java/com/example/library/domain/model/**`
- 实现要求：
  1. 实体：`Reader`(abstract, `@Inheritance(SINGLE_TABLE)`) / `StudentReader` / `TeacherReader`、`BorrowCard`、`BookTitle`、`LibraryItem`(abstract) / `Book` / `Magazine` / `Thesis`、`Loan`、`Reservation`、`BorrowPolicy`、`FineRule`、`FineRecord`、`Librarian`、`SystemAdmin`。
  2. 值对象 `Money`（`BigDecimal` + `cappedAt`）；枚举 `ReaderType`、`ItemType`、`ItemStatus`、`LoanStatus`、`CardStatus`、`ReservationStatus`、`FineStatus`。
  3. 唯一约束：`BorrowCard.cardNo`、`LibraryItem.barcode`、`BookTitle.isbn`、`Reader.idCardNo`。
  4. `Loan` 含 `borrowDate`、`dueDate`、`returnDate`、`status`；提供 `isOverdue(today)`、`overdueDays(today)`、`markReturned(date)`。
  5. 不实现 Controller 与业务流程。
- 验收标准：实体字段与 `13-database-design.md` 一致；`Money` 用 `BigDecimal`；枚举值齐全。
- 建议测试：`MoneyTest`、`LoanTest`（超期判定）

## TASK-003 实现 Repository 端口与持久化适配器

- 目标：领域层定义 Repository 端口，基础设施层用 Spring Data JPA 实现。
- 输入规格：`09-design-model.md` §2.4、`13-database-design.md`
- 允许修改：`src/main/java/com/example/library/domain/repository/**`、`src/main/java/com/example/library/infrastructure/persistence/**`、`src/test/java/com/example/library/infrastructure/**`
- 实现要求：
  1. 端口方法（领域语义）：`LoanRepository.countActiveByReaderId`、`existsOverdueByReaderId`、`findActiveByItemId`、`save`、`findByReaderId`；`BorrowCardRepository.findByCardNo`、`existsByCardNo`；`LibraryItemRepository.findByBarcode`、`existsByBarcode`；`BookTitleRepository.existsByIsbn`、`search`；`ReservationRepository.existsByReaderIdAndTitleIdAndStatus`、`countByReaderIdAndStatus`、`countByTitleIdAndStatus`、`findHeadWaitingByTitleId`。
  2. 适配器类命名 `*RepositoryAdapter`，实现端口并委托 Spring Data 接口。
  3. 编写 `SequenceGeneratorAdapter`（`CARD+年份+6位序号`、`ITEM+年份+6位序号`），保证同进程内唯一。
- 验收标准：端口接口不依赖 Spring；适配器可通过 `@SpringBootTest` 注入。
- 建议测试：`SequenceGeneratorTest`、`LoanRepositoryAdapterTest`

## TASK-004 实现借阅规则策略（BorrowPolicy）

- 目标：实现不同读者类型的借阅数量与期限规则，并可由系统管理员维护。
- 输入规格：`09-design-model.md` §3.1/§3.9、`01-clarifying-questions.md` Q10/Q11、`15-test-plan.md` TC-030
- 允许修改：`src/main/java/com/example/library/domain/model/circulation/**`、`src/main/java/com/example/library/application/AdminAppService.java`（规则维护部分）、`src/test/java/com/example/library/domain/**`
- 实现要求：
  1. `BorrowPolicy.exceedQuota(currentCount)`、`calcDueDate(borrowDate)`。
  2. 初始数据与规格一致：JUNIOR_COLLEGE 5/30、UNDERGRADUATE 5/30、POSTGRADUATE 10/60、DOCTORAL 15/60、TEACHER 20/90。
  3. 规则可按 `readerType` 查询与更新，非法值（≤0、空）抛 `InvalidPolicyValueException`。
  4. 配套单元测试。
- 验收标准：`exceedQuota` 在上限边界行为正确；`calcDueDate` 天数正确。
- 建议测试：`BorrowPolicyTest`（TC-030）

## TASK-005 实现罚款规则策略（FineRule）与罚款计算

- 目标：实现不同借出物类型的罚款规则与计算。
- 输入规格：`09-design-model.md` §3.2/§3.8、`01-clarifying-questions.md` Q19/Q20、`15-test-plan.md` TC-029
- 允许修改：`src/main/java/com/example/library/domain/model/fine/**`、`src/main/java/com/example/library/domain/service/FineCalculator.java`、`src/test/java/com/example/library/domain/fine/**`
- 实现要求：
  1. `FineRule.calcFine(overdueDays)`：`overdueDays = max(0, raw - gracePeriodDays)`；`amount = min(overdueDays × dailyFine, maxFine)`。
  2. 初始数据：CHINESE_BOOK 3/0.20/50、FOREIGN_BOOK 5/0.50/100、CHINESE_MAGAZINE 1/0.50/20、FOREIGN_MAGAZINE 2/1.00/50、THESIS 0/2.00/100。
  3. `FineCalculator.calculate(loan, rule, returnDate)` 返回 `FineRecord` 或空（未超期）。
- 验收标准：`overdueDays<=0` 罚款为 0 且不生成记录；金额受上限约束。
- 建议测试：`FineRuleTest`（TC-029）、`FineCalculatorTest`（TC-025 ~ TC-027 逻辑）

## TASK-006 实现读者注册与查询借阅信息

- 目标：实现读者注册、读者查询本人借阅、管理员按借阅证查询。
- 输入规格：`03-use-cases.md` UC-001/UC-003/UC-103、`14-api-spec.md` §1.1/§2.3
- 允许修改：`src/main/java/com/example/library/application/ReaderAppService.java`、`CirculationAppService.java`（查询部分）、`presentation/controller/ReaderController.java`、`Circular`相关 DTO、`exception/**`、`src/test/java/com/example/library/application/ReaderAppServiceTest.java`
- 实现要求：
  1. 注册：必填校验、证件号唯一；返回 `ReaderResponse`。
  2. 查询：读者仅能查本人；图书管理员可按借阅证号代查；返回含 `overdue` 与 `overdueDays` 的 `LoanQueryResponse`。
  3. 越权抛 `PermissionDeniedException`。
- 验收标准：TC-001、TC-038、TC-039、TC-040 通过。
- 建议测试：`ReaderAppServiceTest`、`ReaderQueryIntegrationTest`

## TASK-007 实现借阅证管理

- 目标：实现办理与注销借阅证（仅系统管理员）。
- 输入规格：`03-use-cases.md` UC-201/UC-202、`14-api-spec.md` §1.2/§1.3
- 允许修改：`application/CardAppService.java`、`presentation/controller/CardController.java`、相关 DTO、`exception/**`、`src/test/java/com/example/library/application/CardAppServiceTest.java`
- 实现要求：
  1. 读者必须存在；一个读者最多一张有效证；`cardNo` 唯一且格式 `CARD+年份+6位序号`。
  2. 注销前检查是否存在未还借阅，存在则拒绝。
  3. 非 `SYSTEM_ADMIN` 抛 `PermissionDeniedException`。
- 验收标准：TC-002、TC-003、TC-004、TC-005 通过。
- 建议测试：`CardAppServiceTest`

## TASK-008 实现图书标题与馆藏副本管理

- 目标：实现题名与副本的添加、删除、检索（维护仅系统管理员）。
- 输入规格：`03-use-cases.md` UC-002/UC-205~UC-208、`14-api-spec.md` §1.4/§4.1~§4.4
- 允许修改：`application/CatalogAppService.java`、`presentation/controller/CatalogController.java`、相关 DTO、`domain/model/catalog/ItemFactory`、`exception/**`、`src/test/java/com/example/library/application/CatalogAppServiceTest.java`
- 实现要求：
  1. ISBN 唯一；`THESIS` 默认 `loanable=false`。
  2. 副本条码唯一且格式 `ITEM+年份+6位序号`；初始状态 `AVAILABLE`。
  3. 删除题名前检查在借副本；删除副本前检查状态非 `ON_LOAN`。
  4. 查询支持题名/作者/ISBN 模糊匹配，返回可借副本数。
- 验收标准：TC-006 ~ TC-011 通过。
- 建议测试：`CatalogAppServiceTest`、`CatalogQueryTest`

## TASK-009 实现办理借书（核心用例）

- 目标：实现 UC-101 全流程与校验链。
- 输入规格：`03-use-cases.md` UC-101、`10-sequence-borrow-book.puml`、`09-design-model.md` §3.1、`15-test-plan.md` TC-012~TC-021
- 允许修改：`domain/service/CirculationService.java`（借书方法）、`application/CirculationAppService.java`（借书方法）、`presentation/controller/CirculationController.java`（borrow 端点）、相关 DTO、`exception/**`、`src/test/java/com/example/library/**` 下借书相关测试
- 实现要求：
  1. 权限：仅 `LIBRARIAN`。
  2. 校验顺序固定：借阅证有效 → 无未缴罚款 → 无超期未还 → 未超额度 → 副本可借且可外借 → 未被他人预约。
  3. 创建 `Loan`，`dueDate = borrowDate + BorrowPolicy.loanPeriodDays`；副本置 `ON_LOAN`；本人为队首预约时预约置 `FULFILLED`。
  4. 应用服务方法加 `@Transactional`。
  5. 不得把校验规则写在 Controller。
- 验收标准：TC-012 ~ TC-021 通过；失败场景不产生脏数据。
- 建议测试：`CirculationServiceBorrowTest`、`BorrowBookIntegrationTest`

## TASK-010 实现办理还书与罚款生成

- 目标：实现 UC-102 全流程，含超期罚款与队首预约保留。
- 输入规格：`03-use-cases.md` UC-102、`11-sequence-return-book.puml`、`09-design-model.md` §3.2、`15-test-plan.md` TC-022~TC-028
- 允许修改：`domain/service/CirculationService.java`（还书方法）、`application/CirculationAppService.java`（还书方法）、`presentation/controller/CirculationController.java`（return 端点）、相关 DTO、`src/test/java/com/example/library/**` 下还书相关测试
- 实现要求：
  1. 权限：仅 `LIBRARIAN`。
  2. 校验：条码存在（本馆藏书）→ 副本在借 → 存在未还 `Loan`。
  3. 关闭 `Loan`（`RETURNED`，写入 `returnDate`）；副本恢复 `AVAILABLE`；有队首预约则置 `RESERVED` 并设置 7 天保留期。
  4. 超期则调用 `FineCalculator` 生成 `FineRecord(UNPAID)`。
  5. 整体 `@Transactional`。
- 验收标准：TC-022 ~ TC-028 通过。
- 建议测试：`CirculationServiceReturnTest`、`ReturnBookIntegrationTest`

## TASK-011 实现预约与取消预约

- 目标：实现 UC-004/UC-005，含排队序号与上限校验。
- 输入规格：`03-use-cases.md` UC-004/UC-005、`12-sequence-reserve-book.puml`、`09-design-model.md` §3.3、`15-test-plan.md` TC-032~TC-037
- 允许修改：`domain/service/ReservationService.java`、`application/ReservationAppService.java`、`presentation/controller/ReservationController.java`、相关 DTO、`src/test/java/com/example/library/**` 下预约测试
- 实现要求：
  1. 权限：读者仅能为本人预约/取消；管理员可代办。
  2. 读者与题名必须存在；同题名同一读者不可重复有效预约；有效预约上限 3。
  3. `queuePosition = 同题名 WAITING 数量 + 1`，`createdAt` 参与排序。
  4. 取消置 `CANCELLED`，仅限本人或系统管理员。
- 验收标准：TC-032 ~ TC-037 通过。
- 建议测试：`ReservationServiceTest`、`ReservationIntegrationTest`

## TASK-012 实现管理员维护（管理员账号与规则维护）

- 目标：实现图书管理员账号维护与借阅/罚款规则维护（仅系统管理员）。
- 输入规格：`03-use-cases.md` UC-203/UC-204/UC-209、`14-api-spec.md` §4.5~§4.7
- 允许修改：`application/AdminAppService.java`、`presentation/controller/AdminController.java`、相关 DTO、`src/test/java/com/example/library/application/AdminAppServiceTest.java`
- 实现要求：
  1. 添加/删除图书管理员：`staffNo` 唯一，删除后不可办理借还。
  2. 规则维护：非法值（负数、0、空）抛 `InvalidPolicyValueException`；更新后对新业务立即生效。
  3. 全部方法校验 `SYSTEM_ADMIN`。
- 验收标准：TC-041、TC-042、TC-043 通过。
- 建议测试：`AdminAppServiceTest`

## TASK-013 实现罚款查询与缴纳

- 目标：实现罚款列表与缴纳（FR-015）。
- 输入规格：`14-api-spec.md` §5、`09-design-model.md` §3.8
- 允许修改：`application/FineAppService.java`、`presentation/controller/FineController.java`、相关 DTO、`src/test/java/com/example/library/application/FineAppServiceTest.java`
- 实现要求：
  1. 列表：读者仅查本人，管理员可查任意读者。
  2. 缴纳：仅 `LIBRARIAN` / `SYSTEM_ADMIN`；缴纳后 `PAID`，此后可正常借书。
- 验收标准：罚款缴纳后 `READER_HAS_UNPAID_FINE` 不再触发。
- 建议测试：`FineAppServiceTest`

## TASK-014 补齐测试、生成 README 与 AI 使用记录

- 目标：对照 `15-test-plan.md` 补齐未覆盖测试，跑通全量测试，补 README 与 AI 使用记录。
- 输入规格：`15-test-plan.md` §4 覆盖矩阵、`19-ai-usage-log.md`
- 允许修改：`src/test/**`、`README.md`、`specs/19-ai-usage-log.md`
- 实现要求：
  1. 输出覆盖分析（已覆盖 / 未覆盖 / 计划），再补测试。
  2. 全量 `mvn -q test` 通过。
  3. README 说明项目简介、技术栈、架构、目录结构、启动与测试方式、核心用例、API、UML 文档、Agent 使用说明；不编造未实现功能。
  4. 按提示词手册模板追加实验二 AI 使用记录。
- 验收标准：测试全绿；README 与实际代码一致；`19-ai-usage-log.md` 含实验二全过程记录。
- 建议测试：`mvn -q test`
