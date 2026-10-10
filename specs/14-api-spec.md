# API 规范（REST API Specification）

> 实现形式：Spring Boot 3.5 + Spring Web（MVC），JSON 请求与响应。
> 统一响应体：成功返回业务 DTO 或 `ApiResponse{code="OK", message, data}`；失败返回 `ErrorResponse{code, message, traceId}`。
> 操作人约定（与 `01-clarifying-questions.md` Q01 一致）：不做真实登录认证，请求头或请求体携带 `X-Operator-Id`、`X-Operator-Role`（`READER` / `LIBRARIAN` / `SYSTEM_ADMIN`）。

## 0. 错误码总表

| HTTP | code | 说明 |
|---|---|---|
| 400 | `CARD_INVALID` | 借阅证不存在/已注销/挂失 |
| 400 | `QUOTA_EXCEEDED` | 超过借阅数量上限 |
| 400 | `READER_HAS_OVERDUE_LOAN` | 存在超期未还图书 |
| 400 | `READER_HAS_UNPAID_FINE` | 存在未缴罚款 |
| 400 | `ITEM_NOT_AVAILABLE` | 副本不可借 |
| 400 | `ITEM_NOT_LOANABLE` | 论文类等不可外借 |
| 400 | `ITEM_RESERVED_BY_OTHER` | 副本被他人预约占用 |
| 400 | `ITEM_NOT_ON_LOAN` | 归还未在借副本 |
| 404 | `ITEM_NOT_FOUND` | 非本馆藏书 |
| 404 | `READER_NOT_FOUND` | 读者不存在 |
| 404 | `TITLE_NOT_FOUND` | 题名不存在 |
| 404 | `LIBRARIAN_NOT_FOUND` | 管理员不存在 |
| 409 | `CARD_ALREADY_EXISTS` | 读者已有有效借阅证 |
| 409 | `CARD_HAS_ACTIVE_LOANS` | 存在未还借阅，不能注销 |
| 409 | `TITLE_ALREADY_EXISTS` | ISBN 重复 |
| 409 | `TITLE_HAS_ACTIVE_LOANS` | 题名下有在借副本 |
| 409 | `DUPLICATE_RESERVATION` | 重复预约同一题名 |
| 409 | `RESERVATION_LIMIT_EXCEEDED` | 有效预约超过 3 条 |
| 409 | `DUPLICATE_BARCODE` | 条码重复 |
| 409 | `ITEM_ON_LOAN_CANNOT_DELETE` | 副本在借不可删除 |
| 422 | `INVALID_POLICY_VALUE` | 规则数值非法 |
| 403 | `PERMISSION_DENIED` | 角色越权 |

## 1. 读者与借阅证

### 1.1 注册读者

```text
POST /api/readers
权限：公开（读者自助注册）
```

请求：

```json
{
  "name": "张三",
  "idCardNo": "20240001",
  "phone": "13800000000",
  "department": "计算机学院",
  "readerType": "UNDERGRADUATE"
}
```

响应 `201`：

```json
{ "readerId": 1, "name": "张三", "readerType": "UNDERGRADUATE", "status": "ACTIVE" }
```

- 成功场景：必填项齐全、证件号唯一 → 创建读者，状态 `ACTIVE`（FR-001）
- 失败场景：字段缺失 → `400` 校验错误；证件号重复 → `400` 提示重复注册
- 覆盖用例：UC-001

### 1.2 办理借阅证

```text
POST /api/admin/borrow-cards
权限：SYSTEM_ADMIN
```

请求：`{ "readerId": 1 }`；响应 `201`：`{ "cardNo": "CARD2026000001", "ownerName": "张三", "department": "计算机学院", "issueDate": "2026-10-10", "status": "VALID" }`

- 成功：读者存在且无有效借阅证 → 生成唯一借阅证号（FR-002）
- 失败：`READER_NOT_FOUND`(404)、`CARD_ALREADY_EXISTS`(409)、`PERMISSION_DENIED`(403)
- 覆盖用例：UC-201

### 1.3 注销借阅证

```text
DELETE /api/admin/borrow-cards/{cardNo}
权限：SYSTEM_ADMIN
```

- 成功：状态置 `REVOKED`，历史借阅保留（FR-003）
- 失败：`CARD_HAS_ACTIVE_LOANS`(409)、`CARD_INVALID`(400)、`PERMISSION_DENIED`(403)
- 覆盖用例：UC-202

### 1.4 查询图书

```text
GET /api/catalog/titles?keyword={题名或作者或ISBN}
权限：公开 / 任意已注册操作人
```

响应 `200`：

```json
[
  { "titleId": 1, "name": "软件体系结构", "author": "某人", "isbn": "978-7-111-00000-0",
    "itemType": "CHINESE_BOOK", "loanable": true, "totalItems": 3, "availableItems": 2 }
]
```

- 成功：按关键字模糊匹配题名/作者/ISBN，返回可借副本数（FR-010）
- 覆盖用例：UC-002

## 2. 流通（借书 / 还书 / 查询借阅）

### 2.1 办理借书

```text
POST /api/circulation/borrow
权限：LIBRARIAN
```

请求：

```json
{ "cardNo": "CARD2026000001", "barcode": "ITEM2026000001" }
```

响应 `200`：

```json
{ "loanId": 10, "readerName": "张三", "titleName": "软件体系结构", "barcode": "ITEM2026000001",
  "borrowDate": "2026-10-10", "dueDate": "2026-11-09", "status": "BORROWED" }
```

- 成功：借阅证有效 → 无未缴罚款 → 无超期未还 → 未超额度 → 副本可借且未被他人预约 → 创建 `Loan`，副本置 `ON_LOAN`（FR-011、BR-003~BR-007、BR-011）
- 失败：`CARD_INVALID`、`READER_HAS_UNPAID_FINE`、`READER_HAS_OVERDUE_LOAN`、`QUOTA_EXCEEDED`、`ITEM_NOT_AVAILABLE`、`ITEM_NOT_LOANABLE`、`ITEM_RESERVED_BY_OTHER`、`PERMISSION_DENIED`
- 覆盖用例：UC-101；顺序图：`10-sequence-borrow-book.puml`

### 2.2 办理还书

```text
POST /api/circulation/return
权限：LIBRARIAN
```

请求：`{ "barcode": "ITEM2026000001" }`

响应 `200`：

```json
{ "loanId": 10, "returnDate": "2026-11-20", "overdueDays": 11, "fineAmount": 2.20, "fineStatus": "UNPAID" }
```

- 成功：副本在本馆且处于借出状态 → 关闭 `Loan`（`RETURNED`）→ 副本置 `AVAILABLE`（或 `RESERVED`，若有排队预约）→ 超期则生成 `FineRecord`（FR-012、FR-015、BR-008~BR-010）
- 失败：`ITEM_NOT_FOUND`(404)、`ITEM_NOT_ON_LOAN`(400)、`PERMISSION_DENIED`(403)
- 覆盖用例：UC-102；顺序图：`11-sequence-return-book.puml`

### 2.3 查询借阅信息

```text
GET /api/readers/{readerId}/loans           权限：本人 READER，或 LIBRARIAN / SYSTEM_ADMIN
GET /api/circulation/readers/{cardNo}/loans 权限：LIBRARIAN（按借阅证号代查）
```

响应 `200`：

```json
[ { "loanId": 10, "titleName": "软件体系结构", "barcode": "ITEM2026000001",
    "borrowDate": "2026-10-10", "dueDate": "2026-11-09", "returnDate": null,
    "status": "BORROWED", "overdue": false, "overdueDays": 0 } ]
```

- 成功：返回当前借阅、历史借阅、超期与罚款信息（FR-013）
- 失败：越权 → `PERMISSION_DENIED`(403)；借阅证无效 → `CARD_INVALID`(400)
- 覆盖用例：UC-003、UC-103

## 3. 预约

### 3.1 预约图书

```text
POST /api/reservations
权限：READER（仅本人），或 LIBRARIAN / SYSTEM_ADMIN 代办
```

请求：`{ "readerId": 1, "titleId": 1 }`

响应 `201`：`{ "reservationId": 5, "titleName": "软件体系结构", "queuePosition": 2, "createdAt": "2026-10-10T10:00:00", "status": "WAITING" }`

- 成功：读者与题名存在、无重复有效预约、有效预约 < 3、分配排队序号（FR-014、BR-010、BR-012）
- 失败：`READER_NOT_FOUND`(404)、`TITLE_NOT_FOUND`(404)、`DUPLICATE_RESERVATION`(409)、`RESERVATION_LIMIT_EXCEEDED`(409)、`PERMISSION_DENIED`(403)
- 覆盖用例：UC-004；顺序图：`12-sequence-reserve-book.puml`

### 3.2 取消预约

```text
DELETE /api/reservations/{reservationId}
权限：预约所属读者本人，或 SYSTEM_ADMIN
```

- 成功：状态置 `CANCELLED`
- 失败：越权 → `PERMISSION_DENIED`
- 覆盖用例：UC-005

## 4. 系统管理员维护

### 4.1 添加图书标题

```text
POST /api/admin/book-titles
权限：SYSTEM_ADMIN
```

请求：

```json
{ "name": "软件体系结构", "author": "某人", "isbn": "978-7-111-00000-0",
  "publisher": "机械工业出版社", "publishedYear": 2024, "itemType": "CHINESE_BOOK" }
```

响应 `201`：`{ "titleId": 1, "name": "软件体系结构", "itemType": "CHINESE_BOOK", "loanable": true }`

- 成功：ISBN 唯一；`THESIS` 类型 `loanable=false`（FR-006）
- 失败：`TITLE_ALREADY_EXISTS`(409)、`PERMISSION_DENIED`(403)
- 覆盖用例：UC-205

### 4.2 删除图书标题

```text
DELETE /api/admin/book-titles/{titleId}
权限：SYSTEM_ADMIN
```

- 成功：题名下无在借副本 → 删除题名与下架副本（FR-007）
- 失败：`TITLE_HAS_ACTIVE_LOANS`(409)、`PERMISSION_DENIED`(403)
- 覆盖用例：UC-206

### 4.3 添加馆藏副本

```text
POST /api/admin/library-items
权限：SYSTEM_ADMIN
```

请求：`{ "titleId": 1, "location": "A区3排2层" }`

响应 `201`：`{ "barcode": "ITEM2026000001", "titleName": "软件体系结构", "itemType": "CHINESE_BOOK", "status": "AVAILABLE" }`

- 成功：题名存在，条码唯一（FR-008）
- 失败：`TITLE_NOT_FOUND`(404)、`DUPLICATE_BARCODE`(409)、`PERMISSION_DENIED`(403)
- 覆盖用例：UC-207

### 4.4 删除馆藏副本

```text
DELETE /api/admin/library-items/{barcode}
权限：SYSTEM_ADMIN
```

- 成功：副本非 `ON_LOAN` → 删除或置 `WITHDRAWN`（FR-009）
- 失败：`ITEM_ON_LOAN_CANNOT_DELETE`(409)、`ITEM_NOT_FOUND`(404)、`PERMISSION_DENIED`(403)
- 覆盖用例：UC-208

### 4.5 维护借阅规则

```text
PUT /api/admin/borrow-policies/{readerType}
权限：SYSTEM_ADMIN
```

请求：`{ "maxLoanCount": 6, "loanPeriodDays": 45 }`

- 成功：更新该读者类型规则，之后新的借书立即生效（FR-016）
- 失败：`INVALID_POLICY_VALUE`(422)（负数/空/0）、`PERMISSION_DENIED`(403)
- 覆盖用例：UC-209

### 4.6 维护罚款规则

```text
PUT /api/admin/fine-rules/{itemType}
权限：SYSTEM_ADMIN
```

请求：`{ "gracePeriodDays": 5, "dailyFine": 0.30, "maxFine": 60 }`

- 成功：更新该借出物类型罚款规则，之后新的还书立即生效（FR-016）
- 失败：`INVALID_POLICY_VALUE`(422)、`PERMISSION_DENIED`(403)
- 覆盖用例：UC-209

### 4.7 维护图书管理员

```text
POST /api/admin/librarians             权限：SYSTEM_ADMIN   （FR-004）
DELETE /api/admin/librarians/{staffNo} 权限：SYSTEM_ADMIN   （FR-005）
```

- 失败：`LIBRARIAN_NOT_FOUND`(404)、`PERMISSION_DENIED`(403)

## 5. 罚款

```text
GET /api/fines?readerId={id}      权限：本人 READER / LIBRARIAN / SYSTEM_ADMIN
POST /api/fines/{fineId}/pay      权限：LIBRARIAN / SYSTEM_ADMIN
```

响应 `200`：`{ "fineId": 3, "loanId": 10, "readerName": "张三", "amount": 2.20, "currency": "CNY", "status": "UNPAID", "createdAt": "2026-11-20T10:00:00" }`

- 成功：列出罚款；缴纳后状态置 `PAID`（FR-015）
- 覆盖用例：UC-210

## 6. 应用服务方法签名（与 REST 一一对应）

| REST 接口 | 应用服务方法 |
|---|---|
| `POST /api/readers` | `ReaderAppService.register(RegisterReaderCommand): ReaderResponse` |
| `GET /api/readers/{id}/loans` | `ReaderAppService.listLoans(readerId, Operator): List<LoanQueryResponse>` |
| `POST /api/admin/borrow-cards` | `CardAppService.issueCard(readerId, Operator): CardResponse` |
| `DELETE /api/admin/borrow-cards/{cardNo}` | `CardAppService.revokeCard(cardNo, Operator): void` |
| `GET /api/catalog/titles` | `CatalogAppService.searchTitles(keyword): List<TitleResponse>` |
| `POST /api/admin/book-titles` | `CatalogAppService.createTitle(CreateTitleCommand, Operator): TitleResponse` |
| `DELETE /api/admin/book-titles/{id}` | `CatalogAppService.deleteTitle(titleId, Operator): void` |
| `POST /api/admin/library-items` | `CatalogAppService.addItem(AddItemCommand, Operator): ItemResponse` |
| `DELETE /api/admin/library-items/{barcode}` | `CatalogAppService.removeItem(barcode, Operator): void` |
| `POST /api/circulation/borrow` | `CirculationAppService.borrowBook(BorrowBookCommand, Operator): LoanResponse` |
| `POST /api/circulation/return` | `CirculationAppService.returnBook(ReturnBookCommand, Operator): ReturnBookResponse` |
| `GET /api/circulation/readers/{cardNo}/loans` | `CirculationAppService.listLoansByCard(cardNo, Operator): List<LoanQueryResponse>` |
| `POST /api/reservations` | `ReservationAppService.reserve(ReserveBookCommand, Operator): ReservationResponse` |
| `DELETE /api/reservations/{id}` | `ReservationAppService.cancel(reservationId, Operator): void` |
| `PUT /api/admin/borrow-policies/{readerType}` | `AdminAppService.updateBorrowPolicy(readerType, UpdateBorrowPolicyCommand, Operator): void` |
| `PUT /api/admin/fine-rules/{itemType}` | `AdminAppService.updateFineRule(itemType, UpdateFineRuleCommand, Operator): void` |
| `POST /api/admin/librarians` | `AdminAppService.addLibrarian(staffNo, name, Operator): void` |
| `DELETE /api/admin/librarians/{staffNo}` | `AdminAppService.removeLibrarian(staffNo, Operator): void` |
| `GET /api/fines` | `FineAppService.listFines(readerId, Operator): List<FineResponse>` |
| `POST /api/fines/{id}/pay` | `FineAppService.pay(fineId, Operator): FineResponse` |

## 7. 权限矩阵

| 接口组 | READER | LIBRARIAN | SYSTEM_ADMIN |
|---|---|---|---|
| 注册读者、查询图书 | 允许（公开） | 允许 | 允许 |
| 查询本人借阅/罚款、预约、取消预约 | 允许（仅本人） | 允许（仅代查） | 允许 |
| 办理借书/还书、按借阅证查询、缴纳罚款 | 禁止（403） | 允许 | 禁止（403） |
| 借阅证、管理员、题名、副本、规则维护 | 禁止（403） | 禁止（403） | 允许 |
