# 领域模型（Domain Model）

## 1. 领域类总览

| 类 | 类别 | 说明 |
|---|---|---|
| Reader | 实体（抽象基类） | 读者，含学生读者与教师读者子类型 |
| StudentReader | 实体（继承 Reader） | 学生读者，含专科生/本科生/研究生/博士生 |
| TeacherReader | 实体（继承 Reader） | 教师读者 |
| BorrowCard | 实体 | 借阅证 |
| Librarian | 实体 | 图书管理员 |
| SystemAdmin | 实体 | 系统管理员 |
| LibraryItem | 实体（抽象基类） | 借出物（馆藏资源） |
| Book | 实体（继承 LibraryItem） | 图书，分中文/外文 |
| Magazine | 实体（继承 LibraryItem） | 杂志，分中文/外文 |
| Thesis | 实体（继承 LibraryItem） | 论文，默认不可外借 |
| BookTitle | 实体 | 书目题名（元数据），与副本为一对多组合 |
| Loan | 实体 | 借阅记录，表达借出/归还/超期状态 |
| Reservation | 实体 | 预约记录，支持排队 |
| BorrowPolicy | 策略/值对象 | 借阅规则（数量、期限） |
| FineRule | 策略/值对象 | 罚款规则（免罚期、每日金额、上限） |
| FineRecord | 实体 | 罚款记录 |
| CirculationService | 领域服务 | 借书/还书流程编排与规则校验 |
| ReservationService | 领域服务 | 预约排队与到书处理 |
| FineCalculator | 领域服务 | 超期罚款计算 |
| Money | 值对象 | 金额（金额 + 币种） |
| DateRange | 值对象 | 借出至应还的时间区间 |
| ItemStatus / LoanStatus / CardStatus / ReservationStatus | 枚举 | 状态类型 |

---

## 2. 核心领域类详解

### 2.1 Reader（读者，抽象实体）

- 职责：表达读者身份与借阅资格来源，是借阅行为的主体。
- 关键属性：`readerId`、`name`、`idCardNo`、`phone`、`department`、`readerType`、`status`
- 关键行为：`isActive()`、`holdCard(BorrowCard)`、`currentLoanCount()`
- 约束：证件号唯一；一个读者最多持有一张有效借阅证（BR-001）。
- 关系：1 对 1 关联 `BorrowCard`；1 对多关联 `Loan`、`Reservation`；由 `BorrowPolicy` 决定借阅额度。

### 2.2 StudentReader（学生读者）

- 职责：表达学生读者及其细分类型（专科生/本科生/研究生/博士生）。
- 关键属性：继承 Reader，增加 `studentNo`、`grade`、`major`
- 关键行为：`getBorrowPolicy()` 按学生细分类型返回借阅规则
- 约束：`studentNo` 唯一。
- 关系：继承 `Reader`。

### 2.3 TeacherReader（教师读者）

- 职责：表达教师读者，适用教师借阅规则。
- 关键属性：继承 Reader，增加 `employeeNo`、`title`、`faculty`
- 关键行为：`getBorrowPolicy()`
- 约束：`employeeNo` 唯一。
- 关系：继承 `Reader`。

### 2.4 BorrowCard（借阅证，实体）

- 职责：作为读者的借阅凭证，承载读者姓名、院系或单位、借阅证号。
- 关键属性：`cardId`、`cardNo`、`readerId`、`ownerName`、`department`、`issueDate`、`status`
- 关键行为：`isValid()`、`revoke()`、`markLost()`
- 约束：`cardNo` 全局唯一且格式 `CARD+年份+6位序号`；`REVOKED`/`LOST` 状态不可借书（BR-002）。
- 关系：多对一关联 `Reader`；被 `Loan` 引用。

### 2.5 Librarian（图书管理员，实体）

- 职责：代理读者办理借书、还书，查询读者借阅信息。
- 关键属性：`librarianId`、`staffNo`、`name`、`status`
- 关键行为：`canOperateCirculation()`
- 约束：`staffNo` 唯一；停用后不可办理借还。
- 关系：与 `Loan` 存在“办理人”关联。

### 2.6 SystemAdmin（系统管理员，实体）

- 职责：维护借阅证、管理员、图书标题、馆藏副本与规则。
- 关键属性：`adminId`、`username`、`name`、`status`
- 关键行为：`canMaintainCatalog()`、`canMaintainPolicy()`
- 关系：与 `BorrowCard`、`BookTitle`、`LibraryItem`、`BorrowPolicy`、`FineRule` 存在维护关联。

### 2.7 BookTitle（图书标题，实体）

- 职责：保存书目元数据（题名/作者/ISBN/出版社/出版年/借出物类型），是检索与预约的对象。
- 关键属性：`titleId`、`name`、`author`、`isbn`、`publisher`、`publishedYear`、`itemType`
- 关键行为：`addItem(LibraryItem)`、`availableItemCount()`、`isLoanable()`
- 约束：ISBN 非空；论文类默认不可外借。
- 关系：组合多个 `LibraryItem`（题名消失时副本失去意义）；被 `Reservation` 引用。

### 2.8 LibraryItem（借出物，抽象实体）

- 职责：表达可借阅的物理馆藏单元，承载条码与可借状态。
- 关键属性：`itemId`、`barcode`、`titleId`、`itemType`、`location`、`status`、`loanable`
- 关键行为：`isAvailable()`、`markOnLoan()`、`markAvailable()`、`markReserved()`、`withdraw()`
- 约束：`barcode` 唯一；只有 `AVAILABLE` 状态可借（BR-006）。
- 关系：多对一关联 `BookTitle`；被 `Loan` 引用；由 `FineRule` 决定罚款规则。

### 2.9 Book / Magazine / Thesis（借出物子类型）

- `Book`：增加 `language`（中文/外文）、`pages`、`edition`。
- `Magazine`：增加 `language`、`issue`（卷期）、`periodicalNo`。
- `Thesis`：增加 `degree`、`supervisor`、`department`；默认 `loanable = false`。
- 关系：均继承 `LibraryItem`，各自适用不同的 `FineRule`。

### 2.10 Loan（借阅记录，实体）

- 职责：记录一次借出—归还的完整生命周期，表达借出、已还、超期状态。
- 关键属性：`loanId`、`cardNo`、`readerId`、`itemId`、`librarianId`、`borrowDate`、`dueDate`、`returnDate`、`status`
- 关键行为：`isOverdue(now)`、`return(now)`、`overdueDays(now)`
- 约束：一个副本同一时刻最多一条未还记录；`status ∈ {BORROWED, OVERDUE, RETURNED}`。
- 关系：关联 `BorrowCard`、`Reader`、`LibraryItem`、`Librarian`；可产生 0..1 条 `FineRecord`。

### 2.11 Reservation（预约记录，实体）

- 职责：表达读者对题名的预约与排队次序。
- 关键属性：`reservationId`、`readerId`、`titleId`、`createdAt`、`queuePosition`、`expireAt`、`status`
- 关键行为：`isHeadOfQueue()`、`cancel()`、`expire(now)`
- 约束：同一读者对同一题名不可重复有效预约；单人有效预约 ≤ 3；保留 7 天（BR-010、BR-012）。
- 关系：多对一关联 `Reader` 与 `BookTitle`。

### 2.12 BorrowPolicy（借阅规则，策略/值对象）

- 职责：按读者类型封装最大借阅数量与借阅期限。
- 关键属性：`policyId`、`readerType`、`maxLoanCount`、`loanPeriodDays`
- 关键行为：`exceedQuota(currentCount)`、`calcDueDate(borrowDate)`
- 约束：数值非负；规则由系统管理员维护。
- 关系：被 `CirculationService` 使用，作用于 `Reader` 的借阅校验。

### 2.13 FineRule（罚款规则，策略/值对象）

- 职责：按借出物类型封装免罚期、每日罚款金额与单笔上限。
- 关键属性：`ruleId`、`itemType`、`gracePeriodDays`、`dailyFine`、`maxFine`
- 关键行为：`calcFine(overdueDays): Money`
- 约束：金额非负；上限必须 ≥ 0。
- 关系：被 `FineCalculator` 使用，作用于 `LibraryItem` 的罚款计算。

### 2.14 FineRecord（罚款记录，实体）

- 职责：记录一次超期罚款及其缴纳状态，作为阻止借书的依据之一。
- 关键属性：`fineId`、`loanId`、`readerId`、`amount`、`currency`、`status`、`createdAt`、`paidAt`
- 关键行为：`pay()`、`isUnpaid()`
- 约束：金额受 `FineRule` 上限约束；未缴罚款阻止借书（BR-005）。
- 关系：一对一关联 `Loan`，多对一关联 `Reader`。

### 2.15 领域服务

- `CirculationService`：编排借书（UC-101）与还书（UC-102）流程，依次调用借阅证校验、罚款校验、超期校验、额度校验、副本可用性校验与预约占用校验；跨聚合操作在此完成。
- `ReservationService`：处理预约创建、排队序号分配、取消、到书通知与过期失效。
- `FineCalculator`：加载 `FineRule`，按 BR-008、BR-009 计算罚款金额，创建 `FineRecord`。

### 2.16 值对象与枚举

- `Money`：金额 + 币种，规避浮点误差，支持比较与加法。
- `DateRange`：借出日期与应还日期构成的时间区间。
- 枚举：`ItemStatus{AVAILABLE, ON_LOAN, RESERVED, WITHDRAWN, LOST, UNDER_REPAIR}`、`LoanStatus{BORROWED, OVERDUE, RETURNED}`、`CardStatus{VALID, REVOKED, LOST}`、`ReservationStatus{WAITING, FULFILLED, CANCELLED, EXPIRED}`、`FineStatus{UNPAID, PAID}`。

---

## 3. 设计说明

1. 领域类来源于业务概念（读者、借阅证、借出物、借阅、预约、规则），不是数据库表的机械翻译。
2. 借阅规则抽象为 `BorrowPolicy`，罚款规则抽象为 `FineRule`，符合“业务规则集中原则”（宪法第 8 条）。
3. 读者类型与借出物类型分别通过继承表达，并配合策略对象实现规则差异，避免条件分支扩散。
4. `Loan` 通过 `status` 与 `dueDate`/`returnDate` 完整表达借出、已还与超期。
5. `Reservation` 通过 `queuePosition` + `createdAt` 支持多人排队与过期顺延。
6. 本图不包含 Controller、Repository、DTO 等设计类，这些属于设计模型（实验二）。
