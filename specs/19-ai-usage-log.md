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

## 4. 实验二预留

实验二将基于本次冻结的 baseline 生成：设计模型 `09-design-model.md`、顺序图 `10/11/12-sequence-*.puml`、API 规范 `14-api-spec.md`、测试计划 `15-test-plan.md`、任务拆解 `16-tasks.md`，并实现代码与自动化测试。每次使用 Agent 均需追加到本文件。
