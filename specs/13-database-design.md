# 数据库概念/逻辑设计（Database Design）

## 1. 持久化对象映射

| 领域类 | 表 | 说明 |
|---|---|---|
| Reader / StudentReader / TeacherReader | `readers` | 单表继承（discriminator = `reader_type`） |
| BorrowCard | `borrow_cards` | 借阅证 |
| Librarian | `librarians` | 图书管理员 |
| SystemAdmin | `system_admins` | 系统管理员 |
| BookTitle | `book_titles` | 图书标题（书目元数据） |
| LibraryItem / Book / Magazine / Thesis | `library_items` | 单表继承（discriminator = `item_type`） |
| Loan | `loans` | 借阅记录 |
| Reservation | `reservations` | 预约记录 |
| BorrowPolicy | `borrow_policies` | 借阅规则 |
| FineRule | `fine_rules` | 罚款规则 |
| FineRecord | `fine_records` | 罚款记录 |

继承映射策略：读者与借出物均采用 **Single Table（单表 + 鉴别列）** 策略，子类专属列允许为空；该策略在教学复杂度与查询性能上最平衡，子类差异字段较少。

---

## 2. 表结构

### 2.1 readers（读者）

| 字段 | 类型 | 空 | 约束 | 默认值 | 说明 |
|---|---|---|---|---|---|
| id | BIGINT | NOT NULL | PK, AUTO_INCREMENT | - | 主键 |
| reader_type | VARCHAR(32) | NOT NULL | 鉴别列 | - | JUNIOR_COLLEGE / UNDERGRADUATE / POSTGRADUATE / DOCTORAL / TEACHER |
| name | VARCHAR(64) | NOT NULL | | - | 姓名 |
| id_card_no | VARCHAR(32) | NOT NULL | UNIQUE | - | 证件号 |
| phone | VARCHAR(20) | NULL | | NULL | 联系方式 |
| department | VARCHAR(64) | NOT NULL | | - | 院系或单位 |
| status | VARCHAR(16) | NOT NULL | | 'ACTIVE' | ACTIVE / INACTIVE |
| student_no | VARCHAR(32) | NULL | UNIQUE | NULL | 学号（学生读者） |
| employee_no | VARCHAR(32) | NULL | UNIQUE | NULL | 工号（教师读者） |
| grade | VARCHAR(16) | NULL | | NULL | 年级（学生） |
| major | VARCHAR(64) | NULL | | NULL | 专业（学生） |
| title | VARCHAR(32) | NULL | | NULL | 职称（教师） |
| created_at | TIMESTAMP | NOT NULL | | CURRENT_TIMESTAMP | 创建时间 |

### 2.2 borrow_cards（借阅证）

| 字段 | 类型 | 空 | 约束 | 默认值 | 说明 |
|---|---|---|---|---|---|
| id | BIGINT | NOT NULL | PK | - | 主键 |
| card_no | VARCHAR(32) | NOT NULL | **UNIQUE** | - | 借阅证号 CARD+年份+6位序号 |
| reader_id | BIGINT | NOT NULL | FK → readers(id), **UNIQUE** | - | 一个读者最多一张有效证 |
| owner_name | VARCHAR(64) | NOT NULL | | - | 读者姓名 |
| department | VARCHAR(64) | NOT NULL | | - | 院系或单位 |
| issue_date | DATE | NOT NULL | | - | 发放日期 |
| status | VARCHAR(16) | NOT NULL | | 'VALID' | VALID / REVOKED / LOST |
| created_at | TIMESTAMP | NOT NULL | | CURRENT_TIMESTAMP | 创建时间 |

> 唯一约束说明：`card_no` 唯一（Q06/BR-001），`reader_id` 唯一保证一证一人。

### 2.3 librarians（图书管理员）

| 字段 | 类型 | 空 | 约束 | 默认值 | 说明 |
|---|---|---|---|---|---|
| id | BIGINT | NOT NULL | PK | - | 主键 |
| staff_no | VARCHAR(32) | NOT NULL | UNIQUE | - | 工号 |
| name | VARCHAR(64) | NOT NULL | | - | 姓名 |
| status | VARCHAR(16) | NOT NULL | | 'ACTIVE' | ACTIVE / DISABLED |
| created_at | TIMESTAMP | NOT NULL | | CURRENT_TIMESTAMP | 创建时间 |

### 2.4 system_admins（系统管理员）

| 字段 | 类型 | 空 | 约束 | 默认值 | 说明 |
|---|---|---|---|---|---|
| id | BIGINT | NOT NULL | PK | - | 主键 |
| username | VARCHAR(64) | NOT NULL | UNIQUE | - | 登录名 |
| name | VARCHAR(64) | NOT NULL | | - | 姓名 |
| status | VARCHAR(16) | NOT NULL | | 'ACTIVE' | ACTIVE / DISABLED |
| created_at | TIMESTAMP | NOT NULL | | CURRENT_TIMESTAMP | 创建时间 |

### 2.5 book_titles（图书标题）

| 字段 | 类型 | 空 | 约束 | 默认值 | 说明 |
|---|---|---|---|---|---|
| id | BIGINT | NOT NULL | PK | - | 主键 |
| name | VARCHAR(200) | NOT NULL | | - | 题名 |
| author | VARCHAR(120) | NULL | | NULL | 作者 |
| isbn | VARCHAR(32) | NOT NULL | UNIQUE | - | ISBN |
| publisher | VARCHAR(120) | NULL | | NULL | 出版社 |
| published_year | INT | NULL | | NULL | 出版年 |
| item_type | VARCHAR(32) | NOT NULL | | - | CN_BOOK / FOREIGN_BOOK / CN_MAGAZINE / FOREIGN_MAGAZINE / THESIS |
| loanable | BOOLEAN | NOT NULL | | TRUE | 是否可外借（论文默认 FALSE） |
| created_at | TIMESTAMP | NOT NULL | | CURRENT_TIMESTAMP | 创建时间 |

### 2.6 library_items（馆藏副本）

| 字段 | 类型 | 空 | 约束 | 默认值 | 说明 |
|---|---|---|---|---|---|
| id | BIGINT | NOT NULL | PK | - | 主键 |
| barcode | VARCHAR(32) | NOT NULL | **UNIQUE** | - | 条码 ITEM+年份+6位序号 |
| title_id | BIGINT | NOT NULL | FK → book_titles(id) | - | 所属题名 |
| item_type | VARCHAR(32) | NOT NULL | 鉴别列 | - | 与题名一致，冗余以支撑子查询 |
| location | VARCHAR(64) | NULL | | NULL | 馆藏地 |
| status | VARCHAR(16) | NOT NULL | | 'AVAILABLE' | AVAILABLE / ON_LOAN / RESERVED / WITHDRAWN / LOST / UNDER_REPAIR |
| loanable | BOOLEAN | NOT NULL | | TRUE | 可借标记 |
| language | VARCHAR(16) | NULL | | NULL | 语言（图书/杂志） |
| pages | INT | NULL | | NULL | 页数（图书） |
| edition | VARCHAR(32) | NULL | | NULL | 版次（图书） |
| issue | VARCHAR(32) | NULL | | NULL | 卷期（杂志） |
| degree | VARCHAR(32) | NULL | | NULL | 学位（论文） |
| supervisor | VARCHAR(64) | NULL | | NULL | 导师（论文） |
| created_at | TIMESTAMP | NOT NULL | | CURRENT_TIMESTAMP | 创建时间 |

### 2.7 loans（借阅记录）

| 字段 | 类型 | 空 | 约束 | 默认值 | 说明 |
|---|---|---|---|---|---|
| id | BIGINT | NOT NULL | PK | - | 主键 |
| card_id | BIGINT | NOT NULL | FK → borrow_cards(id) | - | 借阅证 |
| reader_id | BIGINT | NOT NULL | FK → readers(id) | - | 读者（冗余以加速统计） |
| item_id | BIGINT | NOT NULL | FK → library_items(id) | - | 副本 |
| librarian_id | BIGINT | NOT NULL | FK → librarians(id) | - | 办理人 |
| borrow_date | DATE | NOT NULL | | - | 借出日期 |
| due_date | DATE | NOT NULL | | - | 应还日期 |
| return_date | DATE | NULL | | NULL | 归还日期（未还为 NULL） |
| status | VARCHAR(16) | NOT NULL | | 'BORROWED' | BORROWED / OVERDUE / RETURNED |
| created_at | TIMESTAMP | NOT NULL | | CURRENT_TIMESTAMP | 创建时间 |

> 可区分借出、已还与超期；未还记录约束：`item_id` 上建部分唯一索引保证同一副本仅一条未还记录。

### 2.8 reservations（预约记录）

| 字段 | 类型 | 空 | 约束 | 默认值 | 说明 |
|---|---|---|---|---|---|
| id | BIGINT | NOT NULL | PK | - | 主键 |
| reader_id | BIGINT | NOT NULL | FK → readers(id) | - | 预约人 |
| title_id | BIGINT | NOT NULL | FK → book_titles(id) | - | 预约题名 |
| queue_position | INT | NOT NULL | | - | 排队序号（同题名内递增） |
| created_at | TIMESTAMP | NOT NULL | | CURRENT_TIMESTAMP | 创建时间（排队依据） |
| expire_at | TIMESTAMP | NULL | | NULL | 到书保留截止（7 天） |
| status | VARCHAR(16) | NOT NULL | | 'WAITING' | WAITING / FULFILLED / CANCELLED / EXPIRED |

> 排队支持：`(title_id, status, queue_position)` 索引；同一读者对同一题名的有效唯一约束通过部分唯一索引实现。

### 2.9 borrow_policies（借阅规则）

| 字段 | 类型 | 空 | 约束 | 默认值 | 说明 |
|---|---|---|---|---|---|
| id | BIGINT | NOT NULL | PK | - | 主键 |
| reader_type | VARCHAR(32) | NOT NULL | UNIQUE | - | 读者类型 |
| max_loan_count | INT | NOT NULL | CHECK(> 0) | - | 最大借阅数量 |
| loan_period_days | INT | NOT NULL | CHECK(> 0) | - | 借阅期限（天） |
| updated_at | TIMESTAMP | NULL | | NULL | 更新时间 |

初始数据：专科生 5/30，本科生 5/30，研究生 10/60，博士生 15/60，教师 20/90。

### 2.10 fine_rules（罚款规则）

| 字段 | 类型 | 空 | 约束 | 默认值 | 说明 |
|---|---|---|---|---|---|
| id | BIGINT | NOT NULL | PK | - | 主键 |
| item_type | VARCHAR(32) | NOT NULL | UNIQUE | - | 借出物类型 |
| grace_period_days | INT | NOT NULL | CHECK(>= 0) | 0 | 免罚期（天） |
| daily_fine | DECIMAL(10,2) | NOT NULL | CHECK(>= 0) | - | 每日罚款 |
| max_fine | DECIMAL(10,2) | NOT NULL | CHECK(>= 0) | - | 单笔上限 |
| updated_at | TIMESTAMP | NULL | | NULL | 更新时间 |

初始数据：中文图书 3/0.20/50，外文图书 5/0.50/100，中文杂志 1/0.50/20，外文杂志 2/1.00/50，论文 0/2.00/100。

### 2.11 fine_records（罚款记录）

| 字段 | 类型 | 空 | 约束 | 默认值 | 说明 |
|---|---|---|---|---|---|
| id | BIGINT | NOT NULL | PK | - | 主键 |
| loan_id | BIGINT | NOT NULL | FK → loans(id), UNIQUE | - | 关联借阅记录（一次借阅一笔罚款） |
| reader_id | BIGINT | NOT NULL | FK → readers(id) | - | 罚款对象 |
| amount | DECIMAL(10,2) | NOT NULL | CHECK(>= 0) | - | 罚款金额 |
| currency | VARCHAR(8) | NOT NULL | | 'CNY' | 币种 |
| status | VARCHAR(16) | NOT NULL | | 'UNPAID' | UNPAID / PAID |
| created_at | TIMESTAMP | NOT NULL | | CURRENT_TIMESTAMP | 创建时间 |
| paid_at | TIMESTAMP | NULL | | NULL | 缴纳时间 |

---

## 3. 索引设计

| 索引 | 表 | 字段 | 类型 | 目的 |
|---|---|---|---|---|
| uk_card_no | borrow_cards | card_no | UNIQUE | 借阅证号唯一 |
| uk_card_reader | borrow_cards | reader_id | UNIQUE | 一证一人 |
| uk_item_barcode | library_items | barcode | UNIQUE | 条码唯一 |
| uk_title_isbn | book_titles | isbn | UNIQUE | ISBN 唯一 |
| uk_reader_idcard | readers | id_card_no | UNIQUE | 证件号唯一 |
| uk_policy_reader_type | borrow_policies | reader_type | UNIQUE | 每类型一条规则 |
| uk_finerule_item_type | fine_rules | item_type | UNIQUE | 每类型一条规则 |
| uk_fine_loan | fine_records | loan_id | UNIQUE | 一次借阅一笔罚款 |
| idx_loan_reader_status | loans | (reader_id, status) | INDEX | 统计在借数量、查超期 |
| idx_loan_item_active | loans | (item_id, status) | 部分唯一（status <> 'RETURNED'） | 保证副本仅一条未还记录 |
| idx_loan_due_date | loans | due_date | INDEX | 超期扫描 |
| idx_resv_title_queue | reservations | (title_id, status, queue_position) | INDEX | 排队查询 |
| uk_resv_reader_title | reservations | (reader_id, title_id) | 部分唯一（status = 'WAITING'） | 防重复预约 |
| idx_title_name | book_titles | name | INDEX | 题名检索 |
| idx_item_title_status | library_items | (title_id, status) | INDEX | 可借副本统计 |

## 4. 完整性约束小结

1. 所有外键使用 `ON DELETE RESTRICT`，避免历史借阅/罚款记录被级联删除。
2. 金额一律使用 `DECIMAL(10,2)`，禁止浮点。
3. 状态字段使用 VARCHAR 枚举 + CHECK 约束（或应用层枚举映射）。
4. 借书与还书的多次状态变更必须在同一事务内提交（NFR-004）。
5. 规则表（`borrow_policies`、`fine_rules`）只允许系统管理员更新，变更留 `updated_at` 审计。
