"""
Build the backend BM25 knowledge base from the Android local seed data.

Usage:
    python -m backend.scripts.build_knowledge_base

The Android seed is the canonical source so offline and backend retrieval stay
in sync. The generated JSON is overwritten on each run.
"""
import ast
import json
import os
import re
from pathlib import Path

from loguru import logger


PROJECT_ROOT = Path(__file__).resolve().parents[2]
ANDROID_SEED_PATH = (
    PROJECT_ROOT
    / "app"
    / "src"
    / "main"
    / "java"
    / "com"
    / "mindful"
    / "companion"
    / "data"
    / "database"
    / "KnowledgeSeedData.kt"
)
DEFAULT_OUTPUT_PATH = PROJECT_ROOT / "knowledge" / "knowledge_base.json"


def _resolve_output_path() -> Path:
    configured = os.getenv("KNOWLEDGE_BASE_PATH")
    if not configured:
        return DEFAULT_OUTPUT_PATH
    path = Path(configured)
    return path if path.is_absolute() else PROJECT_ROOT / path


def _read_kotlin_string(expr: str) -> str:
    parts = re.findall(r'"(?:[^"\\]|\\.)*"', expr, flags=re.S)
    return "".join(ast.literal_eval(part) for part in parts)


def _extract_field(body: str, field: str) -> str:
    pattern = rf"{field}\s*=\s*((?:\"(?:[^\"\\]|\\.)*\"\s*\+\s*)*\"(?:[^\"\\]|\\.)*\")"
    match = re.search(pattern, body, flags=re.S)
    if not match:
        raise ValueError(f"Missing field {field}")
    return _read_kotlin_string(match.group(1))


def parse_android_seed(seed_path: Path = ANDROID_SEED_PATH) -> list[dict]:
    text = seed_path.read_text(encoding="utf-8")
    blocks = re.findall(r"KnowledgeEntry\((.*?)\n\s*\)", text, flags=re.S)
    entries: list[dict] = []

    for index, body in enumerate(blocks, start=1):
        entries.append({
            "id": index,
            "category": _extract_field(body, "category"),
            "title": _extract_field(body, "title"),
            "content": _extract_field(body, "content"),
            "keywords": _extract_field(body, "keywords"),
            "source": _extract_field(body, "source"),
            "risk_level_hint": _extract_field(body, "riskLevelHint"),
        })

    if not entries:
        raise RuntimeError(f"No KnowledgeEntry blocks found in {seed_path}")
    titles = [entry["title"] for entry in entries]
    duplicate_titles = {title for title in titles if titles.count(title) > 1}
    if duplicate_titles:
        raise RuntimeError(f"Duplicate knowledge titles: {sorted(duplicate_titles)}")

    return entries


def build_knowledge_base() -> None:
    output_path = _resolve_output_path()
    output_path.parent.mkdir(parents=True, exist_ok=True)

    entries = parse_android_seed()
    with output_path.open("w", encoding="utf-8") as f:
        json.dump(entries, f, ensure_ascii=False, indent=2)
        f.write("\n")

    logger.success(f"Knowledge base saved: {len(entries)} entries -> {output_path}")


if __name__ == "__main__":
    build_knowledge_base()
