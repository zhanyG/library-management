"""实验一 Specs 校验脚本。

校验内容：
1. 必需文件是否存在且非空；
2. PlantUML 文件是否具备 @startuml / @enduml 包裹，且括号配对；
3. 关键文档是否包含实验要求的关键字（用例编号、FR/BR 编号、表名等）。

用法：
    python tools/validate_specs.py

退出码：0 表示全部通过，1 表示存在失败项。
"""

from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parent.parent
SPECS = ROOT / "specs"

REQUIRED_FILES = [
    "00-project-brief.md",
    "01-clarifying-questions.md",
    "02-requirements.md",
    "03-use-cases.md",
    "04-use-case-model.puml",
    "05-domain-model.md",
    "06-domain-class-diagram.puml",
    "07-architecture.md",
    "08-package-diagram.puml",
    "13-database-design.md",
    "17-risk-analysis.md",
    "18-review-checklist.md",
    "19-ai-usage-log.md",
    "constitution.md",
]

PUML_FILES = [
    "04-use-case-model.puml",
    "06-domain-class-diagram.puml",
    "08-package-diagram.puml",
]

KEYWORD_CHECKS = {
    "02-requirements.md": ["FR-001", "FR-016", "BR-001", "BR-015", "验收标准"],
    "03-use-cases.md": ["UC-101", "UC-102", "UC-201", "UC-209", "基本事件流", "异常事件流"],
    "04-use-case-model.puml": ["Student", "Teacher", "Librarian", "SystemAdmin", "<<include>>"],
    "05-domain-model.md": ["BorrowCard", "Loan", "Reservation", "BorrowPolicy", "FineRule", "FineRecord"],
    "06-domain-class-diagram.puml": ["StudentReader", "TeacherReader", "LibraryItem", "Book", "Magazine", "Thesis"],
    "07-architecture.md": ["Presentation", "Application", "Domain", "Infrastructure", "MVC", "事务"],
    "08-package-diagram.puml": ["presentation", "application", "domain", "infrastructure", "dto", "test"],
    "13-database-design.md": [
        "readers", "borrow_cards", "librarians", "system_admins", "book_titles",
        "library_items", "loans", "reservations", "borrow_policies", "fine_rules", "fine_records",
    ],
    "17-risk-analysis.md": ["需求风险", "建模风险", "架构风险", "数据库风险", "权限风险"],
    "18-review-checklist.md": ["需求规格", "用例文本", "用例图", "领域模型", "类图", "架构设计", "包图", "数据库设计"],
}


def check_files(errors):
    print("=== 1. 文件存在性与非空校验 ===")
    for name in REQUIRED_FILES:
        path = SPECS / name
        if not path.exists():
            print(f"[FAIL] 缺失文件: specs/{name}")
            errors.append(f"missing:{name}")
            continue
        size = path.stat().st_size
        lines = len(path.read_text(encoding="utf-8").splitlines())
        if size == 0:
            print(f"[FAIL] 空文件: specs/{name}")
            errors.append(f"empty:{name}")
            continue
        print(f"[ OK ] specs/{name:<38} {size:>7} bytes  {lines:>4} lines")
    print()


def check_puml(errors):
    print("=== 2. PlantUML 基本语法校验 ===")
    for name in PUML_FILES:
        path = SPECS / name
        if not path.exists():
            print(f"[FAIL] 缺失图文件: specs/{name}")
            errors.append(f"missing:{name}")
            continue
        text = path.read_text(encoding="utf-8")
        starts = text.count("@startuml")
        ends = text.count("@enduml")
        brace = text.count("{") - text.count("}")
        paren = text.count("(") - text.count(")")
        problems = []
        if starts != 1 or ends != 1:
            problems.append(f"@startuml/@enduml 数量异常(start={starts},end={ends})")
        if brace != 0:
            problems.append(f"花括号不配对(差 {brace})")
        if paren != 0:
            problems.append(f"圆括号不配对(差 {paren})")
        if problems:
            print(f"[FAIL] specs/{name}: " + "; ".join(problems))
            errors.append(f"puml:{name}")
        else:
            print(f"[ OK ] specs/{name:<38} startuml={starts} enduml={ends} 括号配对正确")
    print()


def check_keywords(errors):
    print("=== 3. 关键内容校验（实验要求的关键概念是否出现） ===")
    for name, keywords in KEYWORD_CHECKS.items():
        path = SPECS / name
        if not path.exists():
            continue
        text = path.read_text(encoding="utf-8")
        missing = [k for k in keywords if k not in text]
        if missing:
            print(f"[FAIL] specs/{name} 缺少关键内容: {', '.join(missing)}")
            errors.append(f"keyword:{name}")
        else:
            print(f"[ OK ] specs/{name:<38} 关键概念齐全（{len(keywords)} 项）")
    print()


def main():
    print(f"Specs 目录: {SPECS}")
    print()
    errors = []
    check_files(errors)
    check_puml(errors)
    check_keywords(errors)

    print("=== 校验结论 ===")
    if errors:
        print(f"存在 {len(errors)} 项问题：{', '.join(errors)}")
        return 1
    print("全部校验通过：文件齐全、非空，PlantUML 语法基本正确，关键概念覆盖完整。")
    print("下一步：按 specs/18-review-checklist.md 进行人工审查，再冻结 baseline。")
    return 0


if __name__ == "__main__":
    sys.exit(main())
