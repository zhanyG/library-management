# AI 使用记录（AI Usage Log）

> 原则：每次使用 Agent 生成 specs、UML、代码或测试，都必须登记，并给出人工审查结论。
> 实验一的完整操作流水由脚本自动生成于项目根目录的 `experiment1-run.log`，本文件记录“谁在何时用 Agent 做了什么、审查结论如何”。

## 1. 实验一 Agent 使用记录

| 序号 | 时间 | Agent | 任务 | 输入（Prompt 摘要） | 产物 | 人工审查结论 |
|---|---|---|---|---|---|---|
| 1 | 实验一 步骤2 | CodeBuddy | 需求澄清 | 阅读 `00-project-brief.md`，只提出澄清问题，覆盖参与者、用例、业务规则、借阅规则、罚款规则、预约规则、权限、数据、架构、测试与教学复杂度，20~30 个，按类别组织 | `specs/01-clarifying-questions.md` | 通过：28 个问题覆盖全部要求类别；已由学生补充人类回答 |
| 2 | 实验一 步骤4 | CodeBuddy | 生成开发宪法 | 基于 Project Brief 与澄清回答生成 `constitution.md`，约束 Agent 行为，强调 UML-as-Spec、人工审查、Baseline、分层与 MVC、规则不入 Controller、权限、测试、Git 审查、AI 记录 | `specs/constitution.md` | 通过：13 条原则，与实验指导示例一致并补充禁止编造原则 |
| 3 | 实验一 步骤5 | CodeBuddy | 生成需求规格 | 基于 Brief、澄清回答、宪法生成 `02-requirements.md`，FR-001~FR-016 + NFR + BR + 验收标准 + 待确认问题 | `specs/02-requirements.md` | 通过：FR 覆盖指导要求的 16 项；每条含验收标准；无“暂不实现”功能 |
| 4 | 实验一 步骤6 | CodeBuddy | 生成用例文本 | 基于 `02-requirements.md` 生成 `03-use-cases.md`，含基本流/备选流/异常流，重点描述借书、还书、预约、办理借阅证、计算罚款 | `specs/03-use-cases.md` | 通过：借书流程 9 步校验齐全；错误码与架构文档一致 |
| 5 | 实验一 步骤7 | CodeBuddy | 生成用例图 | 基于 `03-use-cases.md` 生成 PlantUML 用例图，含 Reader/Student/Teacher/Librarian/SystemAdmin 与 include/extend 关系 | `specs/04-use-case-model.puml` | 通过：参与者泛化正确；关系语义已人工复核 |
| 6 | 实验一 步骤8 | CodeBuddy | 生成领域模型 | 基于需求与用例生成 `05-domain-model.md`，识别核心领域类，含继承/策略/服务/值对象 | `specs/05-domain-model.md` | 通过：含 BorrowCard、Loan、Reservation、BorrowPolicy、FineRule、FineRecord；无设计类污染 |
| 7 | 实验一 步骤9 | CodeBuddy | 生成领域类图 | 基于 `05-domain-model.md` 生成 PlantUML 领域类图，表达属性、方法与关系，排除 Controller/Repository/DTO | `specs/06-domain-class-diagram.puml` | 通过：继承与组合关系清晰；未出现设计类 |
| 8 | 实验一 步骤10 | CodeBuddy | 生成架构设计 | 基于需求、领域模型、宪法生成 `07-architecture.md`，四层分层 + MVC、模块、权限、异常、事务、规则落点、设计模式 | `specs/07-architecture.md` | 通过：层职责清晰；事务边界与异常处理可落地 |
| 9 | 实验一 步骤11 | CodeBuddy | 生成包图 | 基于 `07-architecture.md` 生成 PlantUML 包图，表达分层、模块与依赖方向 | `specs/08-package-diagram.puml` | 通过：Controller 依赖 Application Service，未直接依赖 Repository |
| 10 | 实验一 步骤12 | CodeBuddy | 生成数据库设计 | 基于领域模型与架构生成 `13-database-design.md`，11 张表、字段约束、索引、继承映射策略 | `specs/13-database-design.md` | 通过：card_no/barcode/ISBN 唯一约束齐全；外键与索引完整 |
| 11 | 实验一 步骤13 | CodeBuddy | 生成风险分析与审查清单 | 基于全部 specs 生成 `17-risk-analysis.md` 与 `18-review-checklist.md` | `specs/17-risk-analysis.md`、`specs/18-review-checklist.md` | 通过：风险覆盖需求/建模/架构/数据库/权限/Agent 六类；审查项可人工判断 |
| 12 | 实验一 步骤14 | CodeBuddy | 生成 AI 使用记录 | 汇总本次实验 Agent 使用情况 | `specs/19-ai-usage-log.md` | 通过 |
| 13 | 实验一 全过程 | PowerShell 脚本 `tools/run-experiment1.ps1` | 自动化执行 Git 流程并生成日志 | 创建目录与文件、逐步 git add/commit、校验 PlantUML 与文件完整性、冻结 baseline | `experiment1-run.log` | 通过：日志为系统生成，未人工编辑 |

## 2. 人工审查与修订记录

| 轮次 | 审查对象 | 发现问题 | 处理 |
|---|---|---|---|
| 1 | `05-domain-model.md` | 初稿未显式区分实体/值对象/策略 | 已补充说明，纳入第 2.16 节 |
| 2 | `06-domain-class-diagram.puml` | 初稿未体现 BookTitle 与 LibraryItem 的组合关系 | 已改为 `*--` 组合并标注基数 |
| 3 | `13-database-design.md` | 需确认借阅证号与条码唯一约束 | 已为 `card_no`、`barcode` 增加 UNIQUE，`reader_id` 唯一 |
| 4 | `08-package-diagram.puml` | 需确认 Controller 不直接依赖 Repository | 包图中仅保留 controller → application 依赖，并加 note 说明 |
| 5 | 脚本与文档编码 | 早期脚本用文本方式读写 UTF-8 文档，导致中文双重编码损坏 | 脚本改为字节级备份与恢复（`Copy-Item`），文档重新生成并通过校验脚本验证 |

## 3. 复盘要点

1. Agent 擅长快速产出结构化规格初稿，但取值（借阅数量、期限、罚款）必须由人类确认，否则容易出现编造。
2. 分步骤生成 + 分步骤提交，使每个 commit 可审查、可回溯。
3. UML 以文本（PlantUML）形式纳入 Git，才能做到真正的 UML-as-Spec。
4. 人工审查清单是防止“AI 直接交付”的关键闸门，必须逐项勾选后再冻结 baseline。
5. Windows 环境下用脚本搬运 UTF-8 文档必须做字节级处理，避免 ANSI/UTF-8 转换导致乱码。

---

# 实验二 Agent 使用记录

> 实验二完整操作流水由 `tools/run-experiment2.ps1` 自动生成于项目根目录的 `experiment2-run.log`。
> 本节按“每次使用”为单位登记：使用的 Agent、阶段、任务、Prompt 摘要、产物、测试结果、人工审查结论与 Git 提交。

## 第 14 次使用

- 使用工具：CodeBuddy（Agent）
- 使用阶段：详细设计
- 使用任务：生成详细设计模型 `09-design-model.md`
- 输入 Prompt 摘要：依据 requirements / use-cases / domain-model / architecture / database-design，以用例为单元列出 Controller、Application Service、Domain Service、Entity、Repository、DTO、异常与事务边界，并说明设计模式落点
- Agent 修改文件：`specs/09-design-model.md`
- 测试结果：不适用（文档）
- 人工审查结果：初稿缺少 Controller 命名与指导文档（LibrarianController / FineService）的对应说明、未记录 JPA 注解折中；后续审查轮次已补充 §1 命名对齐、§2.5 实现折中、§3.10 取消预约、§3.11 删除题名
- Git 提交：`generate detailed design model` → `revise detailed design specs after review`

## 第 15 次使用

- 使用工具：CodeBuddy（Agent）
- 使用阶段：UML 建模（顺序图）
- 使用任务：生成办理借书 / 办理还书 / 预约图书三张顺序图
- 输入 Prompt 摘要：按用例文本与详细设计绘制 PlantUML 顺序图，覆盖正常流程与异常分支（借阅证无效、超额度、有超期未还、图书不可借、非本馆藏书、未找到借阅记录、重复预约、超过预约上限）
- Agent 修改文件：`specs/10-sequence-borrow-book.puml`、`specs/11-sequence-return-book.puml`、`specs/12-sequence-reserve-book.puml`
- 测试结果：`tools/validate_specs2.py` 校验通过（alt/else/opt 与 end 配对、关键对象齐全）
- 人工审查结果：发现校验脚本把 `opt` 块误判为不平衡，已修正校验逻辑；顺序图内容本身与用例文本一致
- Git 提交：`generate borrow book sequence diagram`、`generate return book sequence diagram`、`generate reserve book sequence diagram`

## 第 16 次使用

- 使用工具：CodeBuddy（Agent）
- 使用阶段：接口设计与测试设计
- 使用任务：生成 `14-api-spec.md`、`15-test-plan.md`、`16-tasks.md`
- 输入 Prompt 摘要：按 REST 格式定义接口（路径、请求、响应、权限、成功/失败场景），生成 50 条测试用例并给出需求覆盖矩阵，按“一次可完成”粒度拆解 14 个实现任务
- Agent 修改文件：`specs/14-api-spec.md`、`specs/15-test-plan.md`、`specs/16-tasks.md`
- 测试结果：`python tools/validate_specs2.py` 通过（50 个 TC、14 个 TASK、关键要素齐全）
- 人工审查结果：发现接口路径书写空格不统一、测试计划缺少注销借阅证/删除题名/取消预约/目录查询用例，已修订并补充 TC-047 ~ TC-050
- Git 提交：`generate api spec`、`generate test plan`、`generate implementation tasks`

## 第 17 次使用

- 使用工具：CodeBuddy（Agent）
- 使用阶段：编码实现（TASK-001 ~ TASK-003）
- 使用任务：初始化 Spring Boot 项目结构、实现领域实体与 Repository 端口/适配器
- 输入 Prompt 摘要：按 `16-tasks.md` 逐任务实现，先输出计划、只改允许文件、实现后运行测试；领域实体按 `05-domain-model.md` 与 `13-database-design.md` 映射
- Agent 修改文件：`pom.xml`、`src/main/resources/application.yml`、`src/main/java/.../domain/model/**`、`domain/repository/**`、`infrastructure/persistence/**`、`exception/**`、对应测试
- 测试结果：`mvn test` 通过（TASK-001 上下文测试、TASK-002 单元测试、TASK-003 适配器集成测试）
- 人工审查结果：
  1. TASK-002 初版 `MoneyTest.rejectDifferentCurrency` 无法通过公开 API 触发（系统为单一币种），属测试设计缺陷，改为验证金额值相等语义；
  2. `TestDataFactory.addItem` 通过级联保存导致返回实体 id 未回填，已改为显式保存副本；
  3. 生成代码存在导入缺失与 `void` 方法 `return` 语句问题，已修复。
- Git 提交：`complete TASK-001 initialize project structure`、`complete domain entities`、`fix Money value object test case`、`complete repository ports and adapters`

## 第 18 次使用

- 使用工具：CodeBuddy（Agent）
- 使用阶段：编码实现（TASK-004 ~ TASK-008）
- 使用任务：借阅规则策略、罚款规则策略、读者注册与借阅查询、借阅证管理、目录管理
- 输入 Prompt 摘要：按用例与规则取值实现 Strategy（BorrowPolicy / FineRule）、动态权限校验（PermissionChecker）、编号生成（CARD/ITEM + 年份 + 6 位序号）与目录维护
- Agent 修改文件：`domain/model/circulation/BorrowPolicy.java`、`domain/model/fine/FineRule.java`、`application/{ReaderAppService,CardAppService,CatalogAppService,PermissionChecker}.java`、`presentation/controller/{ReaderController,CardController,CatalogController}.java`、规则初始化器与测试
- 测试结果：`mvn test` 通过
- 人工审查结果：规则取值与 `01-clarifying-questions.md` 一致；权限失败场景均有测试覆盖；确认 Controller 未包含业务规则
- Git 提交：`complete borrow policy strategy`、`complete fine rule strategy`、`complete reader registration and loan query`、`complete borrow card management tests`、`complete catalog management`

## 第 19 次使用

- 使用工具：CodeBuddy（Agent）
- 使用阶段：编码实现（TASK-009 ~ TASK-013）
- 使用任务：办理借书、办理还书、预约与取消预约、管理员维护、罚款查询与缴纳
- 输入 Prompt 摘要：按顺序图与详细设计实现校验链与状态变更，事务边界放应用服务层，超期罚款调用 FineCalculator，预约按创建时间排队
- Agent 修改文件：`domain/service/{CirculationService,ReservationService,FineCalculator,ReturnResult}.java`、`application/{CirculationAppService,ReservationAppService,AdminAppService,FineAppService,LoanViewAssembler}.java`、`presentation/controller/{CirculationController,ReservationController,AdminController,FineController}.java`、`domain/repository/ReservationRepository.java` 与适配器、对应集成测试
- 测试结果：`mvn test` 通过（含借书 11 例、还书 8 例、预约 8 例、管理员 7 例、罚款 5 例、REST 端到端 16 例）
- 人工审查结果：发现并修复两个真实缺陷——(1) `CirculationService.returnBook` 先置 `RETURNED` 再计算超期天数，导致超期天数恒为 0、罚款恒为 0（已调整顺序）；(2) 规则维护 DTO 的 `@Positive` 校验先于领域校验，导致非法值返回 400 而非规格约定的 422 `INVALID_POLICY_VALUE`（已把数值合法性统一交由领域实体校验）
- Git 提交：`complete borrow and return use cases`、`complete reservation use case`、`complete admin management`、`complete fine query and payment`

## 第 20 次使用

- 使用工具：CodeBuddy（Agent）
- 使用阶段：测试完善与交付文档（TASK-014）
- 使用任务：REST 端到端测试、README、AI 使用记录
- 输入 Prompt 摘要：对照 `15-test-plan.md` 覆盖矩阵补充端到端测试（状态码、错误码、权限矩阵），生成 README 并追加实验二 AI 使用记录
- Agent 修改文件：`src/test/java/.../presentation/ApiEndToEndTest.java`、`README.md`、`specs/19-ai-usage-log.md`
- 测试结果：`mvn test` 全部通过（96 个测试，0 失败）
- 人工审查结果：确认 README 描述与实现一致、未编造未实现功能；确认 baseline 后仅追加记录类文件（`19-ai-usage-log.md`），未修改已冻结的设计规格
- Git 提交：`complete test coverage and README`、`chore: finalize experiment 2 execution log`

## 实验二复盘

1. 顺序图与详细设计先行，使代码实现阶段几乎没有结构性返工，返工集中在“实现细节缺陷”（如超期天数计算顺序）与“错误码一致性”。
2. 集成测试是发现真实缺陷的主要手段：3 个缺陷（超期天数为 0、DTO 校验覆盖领域错误码、测试数据 id 未回填）全部由测试暴露而非代码评审发现。
3. Agent 生成的批量代码需要“编译—测试—修正”闭环，导入与模板化代码尤其容易出问题。
4. 事务边界放在应用服务层后，失败场景的数据一致性可以直接通过“断言未变”验证，符合 NFR-004。
