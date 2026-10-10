# 测试计划（Test Plan）

> 输入：`02-requirements.md`、`03-use-cases.md`、`09-design-model.md`、`14-api-spec.md`
> 技术栈：Spring Boot 3.5 + JUnit 5 + Spring Boot Test + MockMvc + H2 内存数据库。
> 测试分层：**单元测试**（领域层，无 Spring 上下文）与**集成测试**（应用服务 / REST 接口，含 H2 + MockMvc）。

## 1. 测试范围与分层

| 层次 | 测试类型 | 覆盖对象 | 运行方式 |
|---|---|---|---|
| 领域层 | 单元测试 | `BorrowPolicy`、`FineRule`、`Money`、`Loan`、`LibraryItem` 状态机、`CirculationService` 校验链、`FineCalculator` | `@ExtendWith(SpringExtension.class)` 不加载上下文，纯内存对象 |
| 应用层 | 集成测试 | `CardAppService`、`CatalogAppService`、`CirculationAppService`、`ReservationAppService`、`AdminAppService` | `@SpringBootTest` + `@Transactional`，H2 内存库 |
| 接口层 | 端到端测试 | 全部 REST 接口 | `@SpringBootTest` + `@AutoConfigureMockMvc`，断言 HTTP 状态与 JSON |
| 权限 | 集成测试 | 三类角色越权场景 | 通过 `X-Operator-Role` 传入不同角色 |

## 2. 测试用例

### 2.1 读者与借阅证

#### TC-001 注册读者成功（集成）

- 目标：验证 FR-001 注册功能
- 前置条件：数据库中不存在该证件号
- 输入：`POST /api/readers`，`{name:张三, idCardNo:20240001, department:计算机学院, readerType:UNDERGRADUATE}`
- 步骤：调用接口 → 查询读者
- 预期结果：HTTP `201`，返回 `readerId`，读者状态 `ACTIVE`，类型为 `UNDERGRADUATE`

#### TC-002 办理借阅证成功（集成）

- 目标：验证 FR-002
- 前置条件：读者 `readerId=1` 存在且无有效借阅证
- 输入：`POST /api/admin/borrow-cards`，`{readerId:1}`，header `X-Operator-Role: SYSTEM_ADMIN`
- 预期结果：HTTP `201`，`cardNo` 匹配 `^CARD\d{4}\d{6}$`，状态 `VALID`

#### TC-003 读者不存在时办证失败（集成）

- 输入：`POST /api/admin/borrow-cards`，`{readerId:9999}`
- 预期结果：HTTP `404`，`code=READER_NOT_FOUND`

#### TC-004 重复办证失败（集成）

- 前置条件：读者已持有有效借阅证
- 预期结果：HTTP `409`，`code=CARD_ALREADY_EXISTS`

#### TC-005 非系统管理员办证失败（集成，权限）

- 输入：header `X-Operator-Role: LIBRARIAN`
- 预期结果：HTTP `403`，`code=PERMISSION_DENIED`

### 2.2 目录与馆藏

#### TC-006 添加图书标题成功（集成）

- 输入：`POST /api/admin/book-titles`，ISBN 唯一、`itemType=CHINESE_BOOK`
- 预期结果：HTTP `201`，`loanable=true`

#### TC-007 重复 ISBN 添加标题失败（集成）

- 预期结果：HTTP `409`，`code=TITLE_ALREADY_EXISTS`

#### TC-008 论文类题目标记为不可外借（集成）

- 输入：`itemType=THESIS`
- 预期结果：`loanable=false`

#### TC-009 添加馆藏副本成功（集成）

- 输入：`POST /api/admin/library-items`，`{titleId:1, location:"A区"}`
- 预期结果：HTTP `201`，`barcode` 匹配 `^ITEM\d{4}\d{6}$`，状态 `AVAILABLE`

#### TC-010 普通读者添加馆藏副本失败（集成，权限）

- 预期结果：HTTP `403`，`code=PERMISSION_DENIED`

#### TC-011 删除在借副本失败（集成）

- 前置条件：副本处于 `ON_LOAN`
- 预期结果：HTTP `409`，`code=ITEM_ON_LOAN_CANNOT_DELETE`

### 2.3 借书（UC-101 / FR-011）

#### TC-012 借书成功（集成）

- 前置条件：借阅证 `CARD...` 有效；读者在借 0 本；无罚款；无超期；副本 `AVAILABLE`；无他人预约
- 输入：`POST /api/circulation/borrow`，`{cardNo, barcode}`，header `X-Operator-Role: LIBRARIAN`
- 预期结果：HTTP `200`；`Loan` 状态 `BORROWED`；`dueDate = borrowDate + 该读者类型借阅期限`；副本状态变为 `ON_LOAN`

#### TC-013 借阅证无效时借书失败（集成）

- 前置条件：借阅证已注销（`REVOKED`）或不存在
- 预期结果：HTTP `400`，`code=CARD_INVALID`；无 `Loan` 记录产生

#### TC-014 超过借阅数量时借书失败（集成）

- 前置条件：本科生借阅证，已在借 5 本（`maxLoanCount=5`）
- 预期结果：HTTP `400`，`code=QUOTA_EXCEEDED`；副本状态仍为 `AVAILABLE`

#### TC-015 存在超期未还图书时借书失败（集成）

- 前置条件：读者存在 `dueDate < today` 且未归还的 `Loan`
- 预期结果：HTTP `400`，`code=READER_HAS_OVERDUE_LOAN`

#### TC-016 存在未缴罚款时借书失败（集成）

- 前置条件：读者存在状态 `UNPAID` 的 `FineRecord`
- 预期结果：HTTP `400`，`code=READER_HAS_UNPAID_FINE`

#### TC-017 图书不可借时借书失败（集成）

- 前置条件：副本状态为 `ON_LOAN` 或 `WITHDRAWN`
- 预期结果：HTTP `400`，`code=ITEM_NOT_AVAILABLE`

#### TC-018 论文类不可外借（集成）

- 预期结果：HTTP `400`，`code=ITEM_NOT_LOANABLE`

#### TC-019 副本被他人预约时借书失败（集成）

- 前置条件：题名存在 `WAITING` 预约，队首为其他读者
- 预期结果：HTTP `400`，`code=ITEM_RESERVED_BY_OTHER`

#### TC-020 普通读者不能办理借书（集成，权限）

- 输入：header `X-Operator-Role: READER`
- 预期结果：HTTP `403`，`code=PERMISSION_DENIED`

#### TC-021 借还书状态回滚（集成，事务）

- 目标：验证 NFR-004，借书过程中任一校验失败不产生脏数据
- 步骤：构造超额度条件后调用借书 → 统计 `loans` 与副本状态
- 预期结果：`loans` 记录数不变，副本状态仍为 `AVAILABLE`

### 2.4 还书与罚款（UC-102 / FR-012 / FR-015）

#### TC-022 还书成功（集成）

- 前置条件：副本 `ON_LOAN`，存在未还 `Loan`，未超期
- 输入：`POST /api/circulation/return`，`{barcode}`，header `X-Operator-Role: LIBRARIAN`
- 预期结果：HTTP `200`；`Loan.status=RETURNED`；`returnDate=today`；副本状态 `AVAILABLE`；`fineAmount=0`

#### TC-023 归还非本馆藏书失败（集成）

- 预期结果：HTTP `404`，`code=ITEM_NOT_FOUND`

#### TC-024 归还未在借副本失败（集成）

- 预期结果：HTTP `400`，`code=ITEM_NOT_ON_LOAN`

#### TC-025 超期还书生成罚款（集成）

- 前置条件：`dueDate = today - 20 天`，借出物类型 `CHINESE_BOOK`（免罚期 3 天，每日 0.20，上限 50）
- 预期结果：HTTP `200`；`overdueDays=17`；`fineAmount=3.40`；生成 `FineRecord(status=UNPAID)`

#### TC-026 免罚期内不生成罚款（集成）

- 前置条件：`dueDate = today - 2 天`，`CHINESE_BOOK`（免罚期 3 天）
- 预期结果：`overdueDays=0`，不产生 `FineRecord`

#### TC-027 罚款受单笔上限约束（集成）

- 前置条件：`dueDate = today - 400 天`，`CHINESE_MAGAZINE`（每日 0.50，上限 20）
- 预期结果：`fineAmount=20.00`（等于上限）

#### TC-028 还书后队首预约保留副本（集成）

- 前置条件：题名存在 `WAITING` 预约
- 预期结果：归还后副本状态为 `RESERVED`，队首预约 `expireAt = today + 7 天`

### 2.5 罚款计算单元测试

#### TC-029 FineRule 计算规则（单元）

- 目标：验证 BR-008、BR-009
- 步骤：对 5 种借出物类型分别用 `overdueDays = 0 / 免罚期内 / 超期 / 极大值` 调用 `FineRule.calcFine()`
- 预期结果：`0` / `0` / `overdueDays × dailyFine` / `maxFine`

#### TC-030 BorrowPolicy 规则（单元）

- 目标：验证 BR-003、BR-007
- 步骤：`UNDERGRADUATE(5,30)`、`POSTGRADUATE(10,60)`、`DOCTORAL(15,60)`、`TEACHER(20,90)`、`JUNIOR_COLLEGE(5,30)` 分别调用 `exceedQuota()` 与 `calcDueDate()`
- 预期结果：在借数等于上限时 `exceedQuota=true`；`dueDate` 等于借出日期加对应天数

#### TC-031 Money 值对象（单元）

- 步骤：`Money.of("0.1").plus(Money.of("0.2"))`
- 预期结果：等于 `0.30`（使用 `BigDecimal`，无浮点误差）

### 2.6 预约（UC-004 / FR-014）

#### TC-032 预约图书成功（集成）

- 前置条件：读者存在、题名存在、无有效预约、有效预约数 < 3
- 输入：`POST /api/reservations`，`{readerId:1, titleId:1}`，header `X-Operator-Role: READER`
- 预期结果：HTTP `201`，`status=WAITING`，`queuePosition=1`

#### TC-033 重复预约失败（集成）

- 预期结果：HTTP `409`，`code=DUPLICATE_RESERVATION`

#### TC-034 超过预约上限失败（集成）

- 前置条件：该读者已有 3 条 `WAITING` 预约
- 预期结果：HTTP `409`，`code=RESERVATION_LIMIT_EXCEEDED`

#### TC-035 排队序号按创建时间递增（集成）

- 步骤：读者 A、B、C 依次预约同一题名
- 预期结果：`queuePosition` 分别为 1、2、3

#### TC-036 读者不存在/题名不存在预约失败（集成）

- 预期结果：HTTP `404`，`code=READER_NOT_FOUND` / `TITLE_NOT_FOUND`

#### TC-037 取消他人预约失败（集成，权限）

- 预期结果：HTTP `403`，`code=PERMISSION_DENIED`

### 2.7 查询借阅信息（UC-003 / FR-013）

#### TC-038 读者查询本人借阅成功（集成）

- 预期结果：HTTP `200`，返回当前与历史借阅，含 `overdue` 标记

#### TC-039 图书管理员查询任意读者借阅成功（集成）

- 输入：`GET /api/circulation/readers/{cardNo}/loans`，header `X-Operator-Role: LIBRARIAN`
- 预期结果：HTTP `200`，返回该读者借阅列表

#### TC-040 普通读者查询他人借阅失败（集成，权限）

- 预期结果：HTTP `403`，`code=PERMISSION_DENIED`

### 2.8 规则维护（UC-209 / FR-016）

#### TC-041 修改借阅规则后新借书生效（集成）

- 步骤：`PUT /api/admin/borrow-policies/UNDERGRADUATE`，`{maxLoanCount:6, loanPeriodDays:45}` → 借书
- 预期结果：HTTP `200`；新借书 `dueDate = borrowDate + 45 天`；在借 5 本仍可借第 6 本

#### TC-042 非法规则值被拒绝（集成）

- 输入：`{maxLoanCount:-1, loanPeriodDays:0}`
- 预期结果：HTTP `422`，`code=INVALID_POLICY_VALUE`

#### TC-043 修改罚款规则后新还书生效（集成）

- 步骤：`PUT /api/admin/fine-rules/CHINESE_BOOK`，`{gracePeriodDays:0, dailyFine:1.00, maxFine:30}` → 超期 5 天还书
- 预期结果：罚款 `5.00`

### 2.9 领域服务单元测试

#### TC-044 borrowBook 校验链顺序（单元）

- 步骤：使用内存桩 Repository 构造「借阅证无效」与「超额度」同时成立的场景
- 预期结果：抛出 `CardInvalidException`（借阅证校验优先于额度校验）

#### TC-045 Loan 超期判定（单元）

- 步骤：`dueDate = today - 1` 的 `BORROWED` Loan 调用 `isOverdue(today)` 与 `overdueDays(today)`
- 预期结果：`true`、`1`

#### TC-046 Reservation 队首与过期（单元）

- 步骤：`queuePosition=1` 判定队首；`expireAt` 已过调用 `expire(now)`
- 预期结果：`isHeadOfQueue()=true`；状态变为 `EXPIRED`

## 3. 测试数据准备

- 统一使用 H2 内存库，`ddl-auto=create-drop`，测试类级 `@Transactional` 自动回滚。
- 规则初始数据（`borrow_policies`、`fine_rules`）由 `data.sql` 或 `DataInitializer` 写入，与 `01-clarifying-questions.md` 一致：

| 读者类型 | 上限 | 期限 |  | 借出物类型 | 免罚期 | 每日 | 上限 |
|---|---|---|---|---|---|---|---|
| JUNIOR_COLLEGE | 5 | 30 |  | CHINESE_BOOK | 3 | 0.20 | 50 |
| UNDERGRADUATE | 5 | 30 |  | FOREIGN_BOOK | 5 | 0.50 | 100 |
| POSTGRADUATE | 10 | 60 |  | CHINESE_MAGAZINE | 1 | 0.50 | 20 |
| DOCTORAL | 15 | 60 |  | FOREIGN_MAGAZINE | 2 | 1.00 | 50 |
| TEACHER | 20 | 90 |  | THESIS | 0 | 2.00 | 100 |

## 4. 覆盖矩阵（需求 → 测试）

| 需求 | 测试用例 |
|---|---|
| FR-001 | TC-001 |
| FR-002 / FR-003 | TC-002、TC-003、TC-004、TC-005 |
| FR-004 / FR-005 | 管理端集成测试（TC-005 同组） |
| FR-006 / FR-007 | TC-006、TC-007、TC-008 |
| FR-008 / FR-009 | TC-009、TC-010、TC-011 |
| FR-010 | 目录查询集成测试 |
| FR-011 | TC-012 ~ TC-021 |
| FR-012 | TC-022 ~ TC-024、TC-028 |
| FR-013 | TC-038、TC-039、TC-040 |
| FR-014 | TC-032 ~ TC-037 |
| FR-015 | TC-025 ~ TC-029 |
| FR-016 | TC-041、TC-042、TC-043 |
| NFR-004 | TC-021 |
| BR-003 / BR-007 | TC-014、TC-030、TC-041 |
| BR-004 / BR-005 | TC-015、TC-016 |
| BR-008 / BR-009 | TC-025 ~ TC-027、TC-029 |
| BR-010 / BR-011 / BR-012 | TC-019、TC-028、TC-032 ~ TC-035 |
| BR-013 / BR-014 / BR-015 | TC-005、TC-010、TC-020、TC-037、TC-040 |

## 5. 执行方式

```bash
mvn test                       # 全部单元 + 集成测试
mvn test -Dtest=BorrowPolicyTest   # 单个测试类
mvn -q test                    # 安静模式，仅输出失败信息
```

## 6. 通过标准

1. 全部测试通过，失败数为 0。
2. 核心用例（借书、还书、预约、查询、规则维护）均有成功与失败路径测试。
3. 权限失败场景（读者越权、管理员越权）至少各 1 例通过。
4. 领域层单元测试不依赖 Spring 上下文，可在无数据库情况下运行。
