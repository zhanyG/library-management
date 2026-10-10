# 图书管理系统开发宪法（Constitution）

> 本宪法约束所有 Agent（CodeBuddy / Claude Code / Codex / OpenCode）与人类协作者在生成 Specs、UML、代码、测试时的行为。
> 任何与本宪法冲突的输出，必须被拒绝并重新生成。

## 1. Specs 优先原则

任何代码实现前，必须先完成需求、用例、领域模型、架构、数据库、API、测试和任务拆解文档。无 Specs 不编码。

## 2. Agent 起草，人类确认原则

Agent 可以生成规格、UML、代码初稿，但所有产物必须经过学生人工审查（见 `18-review-checklist.md`）后才能作为实现依据或提交入 baseline。

## 3. UML-as-Spec 原则

用例图、类图、包图、顺序图必须使用 PlantUML（`.puml`）文本格式保存到 `specs/` 目录，纳入 Git 管理；
禁止只提交不可版本管理的图片。所有图必须能被 PlantUML 语法解析。

## 4. Baseline 原则

Specs 审查通过后必须建立 baseline（`git tag experiment1-specs-baseline-v1`）。
实现阶段 Agent 不得擅自修改 specs；确需变更必须走变更流程并在 `19-ai-usage-log.md` 记录理由。

## 5. 分层架构原则

系统必须采用分层架构：表现层（Presentation/Controller）、应用服务层（Application Service）、领域层（Domain）、基础设施层（Infrastructure/Persistence）。
依赖方向只能自上而下，禁止反向依赖；Infrastructure 通过实现 Domain 定义的 Repository 接口接入。

## 6. MVC 原则

Web / 界面层遵循 MVC 分离思想：Controller 只做参数校验、鉴权、DTO 转换与调用应用服务；业务规则一律不得写入 Controller。

## 7. 权限原则

图书管理员、系统管理员、读者权限必须严格区分：
- 借书/还书只能由图书管理员触发；
- 借阅证、管理员、书目、规则维护只能由系统管理员触发；
- 读者只能操作本人数据（查询、预约、取消预约）。
越权访问必须抛出明确的权限异常，不得静默成功。

## 8. 业务规则集中原则

借阅数量、借阅期限、超期罚款等规则集中在 `BorrowPolicy` / `FineRule` 等策略对象与领域服务中实现，不得散落在界面层、Controller 或 SQL 中。

## 9. 测试闭环原则

核心用例必须有自动化测试，至少覆盖：借书成功、借书被借阅规则拒绝、还书超期罚款、预约排队、权限失败场景。

## 10. Git 审查原则

Agent 修改代码后，学生必须查看 `git diff`，确认无越权修改、无删除测试、无绕过规则后再提交。

## 11. AI 使用记录原则

每次使用 Agent 生成 specs、UML、代码或测试，必须记录到 `19-ai-usage-log.md`：记录时间、使用的 Agent、输入 Prompt 摘要、产物文件、人工审查结论。

## 12. 禁止编造原则

Agent 不得编造 Project Brief 与澄清回答之外的需求；对不确定点必须写入“待确认问题”，不得自行假设后直接实现。

## 13. 单一职责与命名原则

类名、方法名、字段名使用英文，注释与文档使用中文；领域概念命名必须与领域模型一致，禁止出现含义不明的缩写。
