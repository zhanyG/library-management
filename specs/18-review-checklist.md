# 人工审查清单（Human Review Checklist）

> 使用方法：逐项人工判断，勾选“通过/不通过”，不通过项必须在修订后重新审查。
> 所有审查结论需记录在 `19-ai-usage-log.md` 的“人工审查结论”列。

## 一、需求规格（02-requirements.md）

- [ ] 1. FR 编号连续，且每条需求都有可验证的验收标准。
- [ ] 2. 未出现 Project Brief 中“暂不实现”的功能（真实扣款、硬件扫描、全文检索、多校区调拨、统一身份认证）。
- [ ] 3. 借阅证相关需求完整（办理、注销、校验）。
- [ ] 4. 明确图书管理员代理借书/还书。
- [ ] 5. 明确区分系统管理员与图书管理员权限。
- [ ] 6. 明确不同读者类型的借阅数量与期限。
- [ ] 7. 明确不同借出物类型的罚款规则。
- [ ] 8. BR 编号连续，且与用例流程中的校验顺序一致。
- [ ] 9. 不确定点已写入“待确认问题”，未被当成已确认需求实现。
- [ ] 10. 需求取值与 `01-clarifying-questions.md` 人类回答一致。

## 二、用例文本（03-use-cases.md）

- [ ] 1. 参与者完整：Reader、StudentReader、TeacherReader、Librarian、SystemAdmin。
- [ ] 2. 每个用例包含编号、名称、主要/次要参与者、前置/后置条件、基本流、备选流、异常流、关联需求。
- [ ] 3. “办理借书”包含：输入借阅证号 → 验证借阅证有效 → 检查未缴罚款 → 检查超期未还 → 检查借阅数量 → 显示读者信息 → 输入副本 → 验证可借 → 创建借阅记录 → 更新副本与读者状态。
- [ ] 4. “办理还书”包含验证是否为本馆藏书、删除或更新借阅信息、超期罚款计算。
- [ ] 5. “预约图书”包含排队规则与取消规则。
- [ ] 6. “办理借阅证”包含读者姓名、院系或单位、借阅证号。
- [ ] 7. “计算超期罚款”包含免罚期、每日金额、上限。
- [ ] 8. 异常流错误码与 `07-architecture.md` 异常表一致。
- [ ] 9. 用例覆盖实验指导中 13 条原始需求。
- [ ] 10. 无读者自助借书/还书的描述（违反 BR-013）。

## 三、用例图（04-use-case-model.puml）

- [ ] 1. 文件以 `@startuml` / `@enduml` 包裹，语法可解析。
- [ ] 2. 参与者包含 Reader、Student、Teacher、Librarian、SystemAdmin。
- [ ] 3. Student 与 Teacher 泛化自 Reader。
- [ ] 4. 用例包含注册账号、查询图书、预约图书、查询借阅信息、办理借书、办理还书、办理借阅证、添加图书、删除图书、维护规则。
- [ ] 5. include / extend 关系使用正确（必然复用用 include，条件发生用 extend）。
- [ ] 6. 系统管理员的维护用例（借阅证、管理员、题名、副本、规则）齐全。
- [ ] 7. 图中不出现与用例文本矛盾的用例名。

## 四、领域模型（05-domain-model.md）

- [ ] 1. 领域类来自业务概念，不是数据库表的机械翻译。
- [ ] 2. 体现不同读者类型（StudentReader / TeacherReader）。
- [ ] 3. 体现不同借出物类型（Book / Magazine / Thesis）。
- [ ] 4. 借阅规则抽象为 `BorrowPolicy`。
- [ ] 5. 罚款规则抽象为 `FineRule`。
- [ ] 6. `Loan` 能表达借出、已还与超期。
- [ ] 7. `Reservation` 支持排队（queuePosition + 创建时间）。
- [ ] 8. 包含 `BorrowCard`、`Loan`、`Reservation`、`FineRecord`。
- [ ] 9. 明确区分实体、值对象、服务/策略。
- [ ] 10. 每个类写明职责、关键属性、关键行为、约束与关系。

## 五、领域类图（06-domain-class-diagram.puml）

- [ ] 1. 类名与 `05-domain-model.md` 完全一致。
- [ ] 2. Reader 与 StudentReader、TeacherReader 继承关系清晰。
- [ ] 3. LibraryItem 与 Book、Magazine、Thesis 继承关系清晰。
- [ ] 4. Reader 与 BorrowCard、Loan、Reservation 关系清晰。
- [ ] 5. BookTitle 与 LibraryItem 的组合关系正确（题名持有副本）。
- [ ] 6. `BorrowPolicy` 与 `FineRule` 的作用表达清晰（策略依赖）。
- [ ] 7. 未出现 Controller、Repository、DTO 等设计类。
- [ ] 8. 关联基数标注合理（1 对 0..1、1 对 0..*）。

## 六、架构设计（07-architecture.md）

- [ ] 1. 明确表现层、应用服务层、领域层、基础设施层、测试层。
- [ ] 2. 每层职责描述清晰且无职责重叠。
- [ ] 3. 模块划分覆盖 reader、card、catalog、circulation、reservation、fine、admin。
- [ ] 4. 权限控制策略区分读者、图书管理员、系统管理员。
- [ ] 5. 异常处理策略包含错误码与统一处理方式。
- [ ] 6. 事务边界明确（应用服务层，借还同一事务）。
- [ ] 7. 明确业务规则落点类（Policy / 领域服务）。
- [ ] 8. 设计模式应用合理（Strategy、Factory、Repository、Service Layer、DTO、MVC）。
- [ ] 9. 满足 MVC：Controller 不含业务规则。
- [ ] 10. 依赖规则明确：Controller 不直接依赖 Repository。

## 七、包图（08-package-diagram.puml）

- [ ] 1. 表达四层分层结构。
- [ ] 2. 包含 presentation、application、domain、infrastructure、dto、test 包。
- [ ] 3. 包含 reader、card、catalog、circulation、reservation、fine、admin 模块包。
- [ ] 4. 依赖方向自上而下，无反向依赖。
- [ ] 5. Controller 依赖 Application Service，未直接依赖 Repository。
- [ ] 6. Infrastructure 实现 Repository 接口（依赖倒置）。
- [ ] 7. Domain 包不依赖任何其他包。

## 八、数据库设计（13-database-design.md）

- [ ] 1. `borrow_cards.card_no` 设置唯一约束。
- [ ] 2. `borrow_cards.reader_id` 唯一（一个读者一张有效证）。
- [ ] 3. `library_items.barcode` 设置唯一约束。
- [ ] 4. `loans` 能区分借出（BORROWED）、已还（RETURNED）、超期（OVERDUE）。
- [ ] 5. `reservations` 支持排队（queue_position + 索引）。
- [ ] 6. `fine_rules` 支持不同借出物类型。
- [ ] 7. `borrow_policies` 支持不同读者类型。
- [ ] 8. 必要外键完整（loans、reservations、fine_records、library_items）。
- [ ] 9. 每张表标注字段类型、是否为空、主键、外键、唯一、默认值。
- [ ] 10. 说明索引设计与继承映射策略。
- [ ] 11. 金额字段使用 DECIMAL 而非浮点。

## 九、整体一致性

- [ ] 1. 所有 `.puml` 文件语法有效。
- [ ] 2. 文档间交叉引用一致（类名、用例编号、错误码）。
- [ ] 3. 所有产物已纳入 Git 且每个步骤有独立 commit。
- [ ] 4. `19-ai-usage-log.md` 记录了每一步 Agent 使用。
- [ ] 5. Baseline 标签 `experiment1-specs-baseline-v1` 已创建。
