"""实验二 Specs 校验脚本。

校验内容：
1. 实验二设计文档是否存在且非空；
2. 顺序图是否为合法 PlantUML（@startuml/@enduml、括号配对、alt/else 分支）；
3. 关键设计要素是否覆盖（Controller/Service/Repository/Policy/异常/事务/设计模式）；
4. 测试计划用例数与任务拆解任务数是否达标。

用法：
    python tools/validate_specs2.py
退出码：0 全部通过，1 存在失败项。
"""

from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parent.parent
SPECS = ROOT / "specs"

DESIGN_FILES = [
    "09-design-model.md",
    "10-sequence-borrow-book.puml",
    "11-sequence-return-book.puml",
    "12-sequence-reserve-book.puml",
    "14-api-spec.md",
    "15-test-plan.md",
    "16-tasks.md",
]

PUML_FILES = [
    "10-sequence-borrow-book.puml",
    "11-sequence-return-book.puml",
    "12-sequence-reserve-book.puml",
]

KEYWORD_CHECKS = {
    "09-design-model.md": [
        "CirculationController", "CirculationAppService", "CirculationService",
        "BorrowCardRepository", "LoanRepository", "LibraryItemRepository",
        "BorrowPolicy", "FineRule", "FineRecord", "FineCalculator",
        "事务", "权限", "DTO", "Strategy", "Repository", "实现折中",
    ],
    "10-sequence-borrow-book.puml": [
        "CirculationController", "CirculationAppService", "CirculationService",
        "BorrowCardRepository", "ReaderRepository", "LoanRepository",
        "LibraryItemRepository", "BorrowPolicy", "CARD_INVALID", "QUOTA_EXCEEDED",
        "READER_HAS_OVERDUE_LOAN", "ITEM_NOT_AVAILABLE", "alt", "else",
    ],
    "11-sequence-return-book.puml": [
        "CirculationController", "CirculationService", "LibraryItemRepository",
        "LoanRepository", "FineRule", "FineCalculator", "FineRecord",
        "ITEM_NOT_FOUND", "ITEM_NOT_ON_LOAN", "alt", "else",
    ],
    "12-sequence-reserve-book.puml": [
        "ReservationController", "ReservationAppService", "ReservationService",
        "ReaderRepository", "BookTitleRepository", "ReservationRepository",
        "READER_NOT_FOUND", "TITLE_NOT_FOUND", "DUPLICATE_RESERVATION",
        "RESERVATION_LIMIT_EXCEEDED", "alt", "else",
    ],
    "14-api-spec.md": [
        "POST /api/circulation/borrow", "POST /api/circulation/return",
        "POST /api/admin/borrow-cards", "POST /api/admin/book-titles",
        "POST /api/admin/library-items", "POST /api/reservations",
        "GET /api/readers/{readerId}/loans", "PUT /api/admin/borrow-policies",
        "PUT /api/admin/fine-rules", "权限", "PERMISSION_DENIED",
    ],
    "15-test-plan.md": [
        "TC-001", "TC-012", "TC-022", "TC-029", "TC-032", "TC-038", "TC-041",
        "单元测试", "集成测试", "覆盖矩阵",
    ],
    "16-tasks.md": [
        "TASK-001", "TASK-004", "TASK-005", "TASK-009", "TASK-010", "TASK-011",
        "TASK-014", "允许修改", "不允许修改", "验收标准", "建议测试",
    ],
}


def check_files(errors):
    print("=== 1. 实验二设计文档存在性与非空校验 ===")
    for name in DESIGN_FILES:
        path = SPECS / name
        if not path.exists():
            print(f"[FAIL] 缺失文件: specs/{name}")
            errors.append(f"missing:{name}")
            continue
        text = path.read_text(encoding="utf-8")
        size = path.stat().st_size
        lines = len(text.splitlines())
        if not text.strip():
            print(f"[FAIL] 空文件: specs/{name}")
            errors.append(f"empty:{name}")
            continue
        print(f"[ OK ] specs/{name:<38} {size:>7} bytes  {lines:>4} lines")
    print()


def check_puml(errors):
    print("=== 2. 顺序图 PlantUML 语法校验 ===")
    for name in PUML_FILES:
        path = SPECS / name
        if not path.exists():
            print(f"[FAIL] 缺失图文件: specs/{name}")
            errors.append(f"missing:{name}")
            continue
        text = path.read_text(encoding="utf-8")
        problems = []
        if text.count("@startuml") != 1 or text.count("@enduml") != 1:
            problems.append("@startuml/@enduml 数量异常")
        if text.count("{") != text.count("}"):
            problems.append("花括号不配对")
        if text.count("(") != text.count(")"):
            problems.append("圆括号不配对")
        alt_count = len(re.findall(r"^\s*alt\s", text, re.M))
        else_count = len(re.findall(r"^\s*else\s", text, re.M))
        end_count = len(re.findall(r"^\s*end\s*$", text, re.M))
        if alt_count == 0:
            problems.append("缺少 alt 分支")
        if alt_count != end_count:
            problems.append(f"alt({alt_count}) 与 end({end_count}) 数量不匹配")
        if problems:
            print(f"[FAIL] specs/{name}: " + "; ".join(problems))
            errors.append(f"puml:{name}")
        else:
            print(f"[ OK ] specs/{name:<38} alt={alt_count} else={else_count} end={end_count} 分支配对正确")
    print()


def check_keywords(errors):
    print("=== 3. 关键设计要素校验 ===")
    for name, keywords in KEYWORD_CHECKS.items():
        path = SPECS / name
        if not path.exists():
            continue
        text = path.read_text(encoding="utf-8")
        missing = [k for k in keywords if k not in text]
        if missing:
            print(f"[FAIL] specs/{name} 缺少: {', '.join(missing)}")
            errors.append(f"keyword:{name}")
        else:
            print(f"[ OK ] specs/{name:<38} 关键要素齐全（{len(keywords)} 项）")
    print()


def check_counts(errors):
    print("=== 4. 测试用例与任务数量校验 ===")
    plan = SPECS / "15-test-plan.md"
    tasks = SPECS / "16-tasks.md"
    if plan.exists():
        cases = sorted(set(re.findall(r"TC-\d{3}", plan.read_text(encoding="utf-8"))))
        print(f"      测试计划用例数: {len(cases)}（{cases[0]} ~ {cases[-1]}）")
        if len(cases) < 30:
            print("[FAIL] 测试计划用例少于 30 条，未达到覆盖要求")
            errors.append("count:test-plan")
        else:
            print("[ OK ] 测试计划用例数量达标")
    if tasks.exists():
        ids = sorted(set(re.findall(r"TASK-\d{3}", tasks.read_text(encoding="utf-8"))))
        print(f"      任务拆解任务数: {len(ids)}（{ids[0]} ~ {ids[-1]}）")
        if len(ids) < 12:
            print("[FAIL] 任务数量少于 12，粒度过大")
            errors.append("count:tasks")
        else:
            print("[ OK ] 任务数量达标")
        text = tasks.read_text(encoding="utf-8")
        if "不允许修改" not in text or "specs" not in text:
            print("[FAIL] 任务拆解未声明“不得修改 specs”约束")
            errors.append("count:tasks-constraint")
    print()


def main():
    print(f"Specs 目录: {SPECS}")
    print()
    errors = []
    check_files(errors)
    check_puml(errors)
    check_keywords(errors)
    check_counts(errors)

    print("=== 校验结论 ===")
    if errors:
        print(f"存在 {len(errors)} 项问题：{', '.join(errors)}")
        return 1
    print("实验二详细设计 Specs 校验通过：文件齐全、顺序图分支完整、关键要素与数量达标。")
    print("下一步：按实验二步骤8 人工审查并修订，再冻结 experiment2-specs-baseline-v1。")
    return 0


if __name__ == "__main__":
    sys.exit(main())
