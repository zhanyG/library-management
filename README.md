# AI 时代图书管理系统（library-management-ai）

《软件设计与体系结构》课程实验项目：以 Spec-Driven 方式，用 Agent 辅助完成需求分析、UML 建模、领域建模、架构设计、数据库设计、详细设计、测试设计与代码实现。

- 实验一：架构设计与 Spec 建模（`specs/00` ~ `specs/19`，baseline 标签 `experiment1-specs-baseline-v1`）
- 实验二：详细设计与 Agent 实现（`specs/09`、`10`~`12`、`14`~`16`，baseline 标签 `experiment2-specs-baseline-v1`）

## 1. 项目简介

系统面向高校图书馆，提供：

- 读者注册与借阅证管理（办理 / 注销）
- 图书题名与馆藏副本维护、图书查询
- **图书管理员代理借书、还书**（读者不能自助借还）
- 借阅信息查询（读者查本人、管理员按借阅证代查）
- 网络预约与排队（到书保留 7 天，超时失效顺延）
- 超期罚款计算与缴纳
- 借阅规则与罚款规则的维护

## 2. 技术栈

| 项 | 版本 / 说明 |
|---|---|
| Java | 17（`maven.compiler.release=17`；本机 JDK 24 运行） |
| 框架 | Spring Boot 3.5.0（Spring Web / Spring Data JPA / Validation） |
| 数据库 | H2 内存库（开发与测试），表结构由 JPA 映射生成 |
| 测试 | JUnit 5 + Spring Boot Test + MockMvc + AssertJ |
| 构建 | Maven |
| 建模 | PlantUML（`specs/*.puml`，UML-as-Spec，纳入 Git） |

## 3. 架构说明

四层分层架构 + MVC（详见 `specs/07-architecture.md`、`specs/08-package-diagram.puml`）：

```text
presentation（Controller + DTO）
        ↓
application（*AppService：用例编排、权限校验、事务边界）
        ↓
domain（实体 / 值对象 / 策略 / 领域服务 / Repository 端口）
        ↑ 实现（依赖倒置）
infrastructure（JPA 接口 + Repository 适配器 + 编号生成 + 规则初始化）
```

关键约束：

- Controller 只做参数校验、请求头解析与响应封装，**不含业务规则**，**不直接依赖 Repository**
- 事务边界只在应用服务层（`@Transactional`）；借书、还书的多处状态变更在同一事务内
- 借阅规则集中 `BorrowPolicy`（Strategy），罚款规则集中 `FineRule`（Strategy）
- 领域层定义 Repository 端口（`domain/repository`），基础设施层以适配器实现

## 4. 目录结构

```text
library-management-ai/
  pom.xml
  README.md
  experiment1-run.log                 # 实验一执行日志（系统生成）
  experiment2-run.log                 # 实验二执行日志（系统生成）
  specs/                              # 全部规格（实验一 + 实验二）
    00-project-brief.md ... 19-ai-usage-log.md
    constitution.md
  tools/
    run-experiment1.ps1               # 实验一自动化脚本（Git 流程 + 日志）
    validate_specs.py                 # 实验一 Specs 校验
    run-experiment2.ps1               # 实验二自动化脚本
    validate_specs2.py                # 实验二 Specs 校验
  src/main/java/com/example/library/
    LibraryApplication.java
    presentation/controller/          # ReaderController、CardController、CatalogController、
                                      # CirculationController、ReservationController、
                                      # AdminController、FineController
    presentation/dto/                 # 请求与响应记录类
    application/                      # 7 个应用服务 + Operator/PermissionChecker/LoanViewAssembler
    domain/model/{reader,catalog,circulation,reservation,fine}/
    domain/service/                   # CirculationService、ReservationService、FineCalculator、NumberGenerator
    domain/repository/                # 领域 Repository 端口
    infrastructure/persistence/       # Jpa*Repository、*RepositoryAdapter、SequenceGeneratorAdapter、规则初始化
    exception/                        # BusinessException 体系 + GlobalExceptionHandler
  src/test/java/com/example/library/
    domain/                           # 单元测试（策略、金额、借阅状态）
    application/                      # 应用服务集成测试（借书/还书/预约/查询/维护/罚款）
    infrastructure/persistence/       # 适配器与编号生成测试
    presentation/ApiEndToEndTest.java # REST 端到端测试（状态码、错误码、权限）
    support/TestDataFactory.java
```

## 5. 启动方式

```bash
mvn spring-boot:run          # 默认端口 8080，H2 内存库
```

启动时会自动写入初始规则数据（各读者类型借阅规则、各借出物类型罚款规则）。

H2 控制台：`http://localhost:8080/h2-console`（JDBC URL：`jdbc:h2:mem:library`，用户 `sa`，密码为空）。

接口认证约定（与 `specs/01-clarifying-questions.md` Q01 一致，第一阶段不做真实登录）：请求头携带

```text
X-Operator-Id: 操作人 ID
X-Operator-Role: READER | LIBRARIAN | SYSTEM_ADMIN
```

示例：

```bash
# 注册读者
curl -X POST http://localhost:8080/api/readers -H "Content-Type: application/json" \
  -d '{"name":"张三","idCardNo":"20240001","department":"计算机学院","readerType":"UNDERGRADUATE"}'

# 系统管理员办理借阅证
curl -X POST http://localhost:8080/api/admin/borrow-cards -H "Content-Type: application/json" \
  -H "X-Operator-Id: 1" -H "X-Operator-Role: SYSTEM_ADMIN" -d '{"readerId":1}'

# 图书管理员办理借书
curl -X POST http://localhost:8080/api/circulation/borrow -H "Content-Type: application/json" \
  -H "X-Operator-Id: 7" -H "X-Operator-Role: LIBRARIAN" \
  -d '{"cardNo":"CARD2026000001","barcode":"ITEM2026000001"}'
```

## 6. 测试方式

```bash
mvn test                              # 全量测试（单元 + 集成 + REST 端到端）
mvn test -Dtest=BorrowPolicyTest      # 单个测试类
```

测试分层对应 `specs/15-test-plan.md`：

| 层次 | 代表测试类 | 覆盖内容 |
|---|---|---|
| 领域单元测试 | `MoneyTest`、`LoanTest`、`BorrowPolicyTest`、`FineRuleTest` | 金额计算、超期判定、借阅规则、罚款规则 |
| 应用集成测试 | `BorrowBookAppServiceTest`、`ReturnBookAppServiceTest`、`ReservationAppServiceTest`、`ReaderAppServiceTest`、`CardAppServiceTest`、`CatalogAppService` 相关、`AdminAppServiceTest`、`FineAppServiceTest` | 核心用例成功路径与异常路径、权限失败、事务一致性 |
| 基础设施测试 | `SequenceGeneratorAdapterTest`、`RepositoryAdapterIntegrationTest` | 编号格式与唯一性、端口方法语义 |
| REST 端到端 | `ApiEndToEndTest` | HTTP 状态码、错误码、权限矩阵 |

## 7. 核心用例与实现落点

| 用例 | 应用服务 | 领域服务 | 说明 |
|---|---|---|---|
| 办理借书 UC-101 | `CirculationAppService.borrowBook` | `CirculationService.borrowBook` | 校验链：借阅证有效 → 无未缴罚款 → 无超期未还 → 未超额度 → 副本可借 → 未被他人预约 |
| 办理还书 UC-102 | `CirculationAppService.returnBook` | `CirculationService.returnBook` + `FineCalculator` | 关闭借阅、恢复副本、超期生成罚款、队首预约保留 7 天 |
| 预约图书 UC-004 | `ReservationAppService.reserve` | `ReservationService.reserve` | 排队序号 = 同题名 WAITING 数量 + 1，单人有效预约上限 3 |
| 取消预约 UC-005 | `ReservationAppService.cancel` | `ReservationService.cancel` | 仅本人或系统管理员 |
| 查询借阅 UC-003/103 | `ReaderAppService.listLoans`、`CirculationAppService.listLoansByCard` | — | 读者仅本人，管理员可代查 |
| 办理借阅证 UC-201 | `CardAppService.issueCard` | `NumberGenerator` | 证号 `CARD+年份+6位序号`，一证一人 |
| 目录维护 UC-205~208 | `CatalogAppService` | — | ISBN / 条码唯一，在借副本不可删 |
| 罚款 UC-210 | `FineAppService` | `FineCalculator` + `FineRule` | 超期天数 = 归还日 − 应还日 − 免罚期；金额受上限约束 |
| 规则维护 UC-209 | `AdminAppService` | `BorrowPolicy`、`FineRule` | 更新后对新业务立即生效 |

## 8. 核心 API

| 方法 | 路径 | 权限 |
|---|---|---|
| POST | `/api/readers` | 公开 |
| GET | `/api/readers/{readerId}/loans` | 本人 / 图书管理员 / 系统管理员 |
| GET | `/api/catalog/titles?keyword=` | 公开 |
| POST | `/api/circulation/borrow` | 图书管理员 |
| POST | `/api/circulation/return` | 图书管理员 |
| GET | `/api/circulation/readers/{cardNo}/loans` | 图书管理员 |
| POST | `/api/reservations` | 读者本人（或管理员代办） |
| DELETE | `/api/reservations/{reservationId}` | 预约所属读者 / 系统管理员 |
| POST / DELETE | `/api/admin/borrow-cards`、`/api/admin/borrow-cards/{cardNo}` | 系统管理员 |
| POST / DELETE | `/api/admin/book-titles`、`/api/admin/book-titles/{titleId}` | 系统管理员 |
| POST / DELETE | `/api/admin/library-items`、`/api/admin/library-items/{barcode}` | 系统管理员 |
| PUT | `/api/admin/borrow-policies/{readerType}` | 系统管理员 |
| PUT | `/api/admin/fine-rules/{itemType}` | 系统管理员 |
| POST / DELETE | `/api/admin/librarians`、`/api/admin/librarians/{staffNo}` | 系统管理员 |
| GET / POST | `/api/fines?readerId=`、`/api/fines/{fineId}/pay` | 本人或管理员 / 管理员 |

完整请求响应、错误码与权限矩阵见 `specs/14-api-spec.md`。

## 9. UML 与规格文档说明

所有图均以 PlantUML 文本保存，纳入 Git（UML-as-Spec）：

| 文件 | 内容 |
|---|---|
| `specs/04-use-case-model.puml` | 用例图 |
| `specs/06-domain-class-diagram.puml` | 领域类图 |
| `specs/08-package-diagram.puml` | 分层包图 |
| `specs/10-sequence-borrow-book.puml` | 办理借书顺序图（含 5 类异常分支） |
| `specs/11-sequence-return-book.puml` | 办理还书顺序图（含罚款计算分支） |
| `specs/12-sequence-reserve-book.puml` | 预约图书顺序图（含排队与重复预约分支） |

校验脚本：

```bash
python tools/validate_specs.py     # 实验一 Specs 校验
python tools/validate_specs2.py    # 实验二 Specs 校验
```

## 10. Agent 使用说明

- 全部 Specs 由 Agent（CodeBuddy）起草、经人工审查后冻结；冻结后实现阶段不修改 `specs/`。
- 执行过程由脚本自动记录（非人工编辑）：
  - `experiment1-run.log`：`powershell -ExecutionPolicy Bypass -File tools/run-experiment1.ps1`
  - `experiment2-run.log`：`powershell -ExecutionPolicy Bypass -File tools/run-experiment2.ps1 -Phase specs|revise|task|final`
- 每次 Agent 使用均登记于 `specs/19-ai-usage-log.md`，含 Prompt 摘要、产物、测试结果与人工审查结论。
- 实现按 `specs/16-tasks.md` 的 TASK-001 ~ TASK-014 逐步提交，Git 历史可按任务回溯。

## 11. 已知限制

- 第一阶段不做真实登录认证，操作人角色由请求头传入（测试覆盖越权场景）。
- 遗失/损坏赔偿、续借、预约外部通知未实现（列入 `specs/02-requirements.md` 待确认问题）。
- 权限校验依赖调用方如实传入请求头，生产环境需接入认证网关。
