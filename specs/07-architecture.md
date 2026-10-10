# 架构设计说明书（Architecture Design）

## 1. 架构总览

系统采用**四层分层架构 + MVC**：

```text
┌──────────────────────────────────────────────┐
│ Presentation Layer（Controller / View）      │  MVC 中的 C 与 V
├──────────────────────────────────────────────┤
│ Application Service Layer（用例编排）         │  用例流程、事务边界、DTO 转换
├──────────────────────────────────────────────┤
│ Domain Layer（实体、值对象、领域服务、策略）  │  业务规则核心
├──────────────────────────────────────────────┤
│ Infrastructure Layer（Repository 实现、持久化）│  技术实现细节
└──────────────────────────────────────────────┘
              ▲ Test Layer 横切所有层
```

技术选型（实验二实现阶段确定其一）：Java 17 + Spring Boot 3.x + Spring Data JPA + H2/MySQL + JUnit5 + MockMvc，
或 Python 3.10+ + FastAPI + SQLAlchemy + SQLite + pytest + httpx。

## 2. 各层职责

### 2.1 Presentation Layer（表现层 / Controller）

- 职责：接收 HTTP/CLI 请求，参数校验与绑定，构造 DTO，调用应用服务，统一封装响应与异常码。
- 主要组件：`ReaderController`、`CardController`、`CatalogController`、`CirculationController`、`ReservationController`、`FineController`、`AdminController`。
- 禁止：不得包含业务规则（宪法第 6 条）；不得直接依赖 Repository；不得直接访问数据库。

### 2.2 Application Service Layer（应用服务层）

- 职责：用例级流程编排（借书、还书、预约、办理借阅证等），事务边界定义，权限前置校验，领域对象 ↔ DTO 转换，领域事件发布。
- 主要组件：`CirculationAppService`、`ReservationAppService`、`CatalogAppService`、`CardAppService`、`AdminAppService`、`FineAppService`。
- 禁止：不得把业务规则写在本层，规则必须下沉到领域层。

### 2.3 Domain Layer（领域层）

- 职责：承载全部业务规则与不变量。
- 主要组件：
  - 实体：`Reader`/`StudentReader`/`TeacherReader`、`BorrowCard`、`BookTitle`、`LibraryItem`/`Book`/`Magazine`/`Thesis`、`Loan`、`Reservation`、`FineRecord`、`Librarian`、`SystemAdmin`
  - 值对象：`Money`、`DateRange`
  - 策略：`BorrowPolicy`、`FineRule`
  - 领域服务：`CirculationService`、`ReservationService`、`FineCalculator`
  - 仓储接口：`ReaderRepository`、`LoanRepository`、`ItemRepository`、`ReservationRepository`、`PolicyRepository`（仅接口）
- 禁止：不得依赖 Spring、Servlet、JDBC 等框架或技术细节。

### 2.4 Infrastructure / Persistence Layer（基础设施层）

- 职责：实现 Domain 层定义的 Repository 接口；ORM 映射；事务管理；编号生成器（借阅证号、条码）；外部通知。
- 主要组件：`JpaLoanRepository`、`JpaItemRepository`、`SequenceGenerator`、`PersistenceConfig`。
- 约束：只能实现 Domain 接口，不得反向侵入业务规则。

### 2.5 Test Layer（测试层）

- 单元测试：领域层（策略、罚款计算、借阅校验）纯内存测试。
- 集成测试：应用服务 + 内存数据库。
- API 测试：MockMvc / httpx 对核心用例端到端验证。
- 覆盖要求：借书成功、借书被规则拒绝、还书超期罚款、预约排队、权限失败五类场景必测。

## 3. 主要模块划分

| 模块 | 内容 | 主要类 |
|---|---|---|
| reader | 读者注册、类型与规则查询 | `Reader`、`StudentReader`、`TeacherReader`、`ReaderService` |
| card | 借阅证办理、注销、校验 | `BorrowCard`、`CardAppService` |
| catalog | 题名与副本维护、检索 | `BookTitle`、`LibraryItem`、`Book`、`Magazine`、`Thesis`、`CatalogAppService` |
| circulation | 借书、还书、借阅查询 | `Loan`、`CirculationService`、`CirculationAppService` |
| reservation | 预约、排队、取消、过期 | `Reservation`、`ReservationService`、`ReservationAppService` |
| fine | 超期罚款计算与缴纳 | `FineRule`、`FineRecord`、`FineCalculator`、`FineAppService` |
| admin | 管理员维护、规则维护 | `SystemAdmin`、`Librarian`、`AdminAppService`、`BorrowPolicy`、`FineRule` |

## 4. 权限控制策略

| 角色 | 可执行 | 禁止 |
|---|---|---|
| Reader | 注册、查询图书、查询本人借阅、预约/取消预约 | 借书、还书、访问他人数据 |
| Librarian | 借书、还书、按借阅证查询读者借阅信息 | 维护书目、管理员、规则 |
| SystemAdmin | 借阅证办理/注销、管理员维护、书目与副本维护、规则维护 | 直接办理借还（不代理借阅） |

- 实现方式：Controller 入口解析操作人 ID 与角色 → 应用服务执行 `assertRole(...)` 前置断言 → 领域服务仅接受已完成鉴权的操作人参数。
- 越权必须抛出 `PermissionDeniedException`，返回明确错误码，严禁静默处理。

## 5. 异常处理策略

| 异常 | 触发场景 | 错误码 |
|---|---|---|
| `CardInvalidException` | 借阅证不存在/已注销/挂失 | `CARD_INVALID` |
| `QuotaExceededException` | 超过借阅数量上限 | `QUOTA_EXCEEDED` |
| `OverdueLoanExistsException` | 存在超期未还图书 | `READER_HAS_OVERDUE_LOAN` |
| `UnpaidFineExistsException` | 存在未缴罚款 | `READER_HAS_UNPAID_FINE` |
| `ItemNotAvailableException` | 副本不可借 | `ITEM_NOT_AVAILABLE` |
| `ItemReservedByOtherException` | 副本被他人预约占用 | `ITEM_RESERVED_BY_OTHER` |
| `ItemNotOnLoanException` | 归还未在借的副本 | `ITEM_NOT_ON_LOAN` |
| `ItemNotFoundException` | 非本馆藏书 | `ITEM_NOT_FOUND` |
| `PermissionDeniedException` | 角色越权 | `PERMISSION_DENIED` |
| `DuplicateReservationException` | 重复预约 | `DUPLICATE_RESERVATION` |
| `ReservationLimitExceededException` | 超过预约上限 | `RESERVATION_LIMIT_EXCEEDED` |
| `InvalidPolicyValueException` | 规则数值非法 | `INVALID_POLICY_VALUE` |

- 统一由 `GlobalExceptionHandler`（表现层）捕获并转换为标准响应体 `{"code","message","traceId"}`。
- 领域层抛出业务异常，不抛技术异常；技术异常由基础设施层包装为 `InfrastructureException`。

## 6. 事务边界

- 事务定义在**应用服务层**方法上（`@Transactional` / 显式事务上下文）。
- 借书：校验 → 创建 Loan → 更新副本状态 → 更新预约状态，必须同一事务（NFR-004）。
- 还书：登记归还 → 计算并写入罚款 → 更新副本状态 → 处理队首预约，必须同一事务。
- 领域层保持纯内存状态变更，不自行开启事务。

## 7. 业务规则落点

| 规则 | 落点类 |
|---|---|
| 借阅数量、借阅期限 | `BorrowPolicy` + `CirculationService` |
| 超期判定 | `Loan.isOverdue()` / `Loan.overdueDays()` |
| 罚款计算 | `FineRule.calcFine()` + `FineCalculator` |
| 副本可借性 | `LibraryItem.isAvailable()` |
| 预约排队与过期 | `ReservationService` + `Reservation` |
| 借阅证有效性 | `BorrowCard.isValid()` |
| 权限 | 应用服务 `assertRole` + 表现层拦截器 |

## 8. 设计模式应用

| 模式 | 应用点 |
|---|---|
| Strategy | `BorrowPolicy`（不同读者类型）、`FineRule`（不同借出物类型） |
| Factory | `ReaderFactory`（创建学生/教师读者）、`LibraryItemFactory`（创建图书/杂志/论文） |
| Repository | 领域对象持久化抽象，实现在 Infrastructure |
| Service Layer | 应用服务组织用例流程与事务 |
| DTO | 隔离接口层与领域层 |
| MVC | Controller / 视图 / 模型分离 |
| Template Method | 借书校验链（校验步骤固定，规则可替换） |
| Value Object | `Money`、`DateRange` |

## 9. 依赖规则

1. 依赖方向只能自上而下：Presentation → Application → Domain ← Infrastructure。
2. Controller 不得直接依赖 Repository（宪法第 5 条）。
3. Domain 不依赖任何层，也不依赖框架。
4. Infrastructure 通过实现 Domain 的 Repository 接口接入（依赖倒置）。
5. 跨模块调用必须经应用服务，不得跨模块直接访问其他模块的 Repository。

## 10. 与实验一的衔接

本架构文档与 `08-package-diagram.puml` 共同构成实验二的包结构依据；实验二将在此基础上生成设计类图、顺序图（借书/还书/预约）、API 规范、测试计划与任务拆解。
