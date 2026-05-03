"""
知识库 BM25 索引验证脚本（替代原向量化流程）
用法: python -m backend.vectorization.embed_knowledge

加载 knowledge_base.json，构建 BM25 索引，运行测试查询验证检索效果
无需 GPU、无需 Embedding API，纯本地 CPU 运行
"""
import os
import json
from pathlib import Path
from dotenv import load_dotenv
from loguru import logger

load_dotenv()

import jieba
from rank_bm25 import BM25Okapi


TEST_QUERIES = [
    "最近很焦虑，控制不住担心",
    "睡眠很差，失眠怎么办",
    "感觉很绝望，活不下去",
    "认知行为疗法怎么用",
    "和同学关系很差，人际关系困扰",
    "考试压力太大",
]


def document_text(entry: dict) -> str:
    title = entry.get("title", "")
    keywords = entry.get("keywords", "")
    category = entry.get("category", "")
    content = entry.get("content", "")
    return f"{title} {title} {keywords} {keywords} {keywords} {category} {content}"


def build_and_validate():
    configured_path = os.getenv("KNOWLEDGE_BASE_PATH")
    project_root = Path(__file__).resolve().parents[2]
    if configured_path:
        kb_file = Path(configured_path)
        if not kb_file.is_absolute():
            kb_file = project_root / kb_file
    else:
        kb_file = project_root / "knowledge" / "knowledge_base.json"

    # 1. 加载知识库
    if not kb_file.exists():
        logger.error(f"Knowledge base not found: {kb_file}")
        logger.info("Please run: python -m backend.scripts.build_knowledge_base")
        return

    with open(kb_file, "r", encoding="utf-8") as f:
        entries = json.load(f)
    logger.info(f"Loaded {len(entries)} knowledge entries from {kb_file}")

    # 2. 构建 BM25 索引
    tokenized_corpus = [
        list(jieba.cut(document_text(e)))
        for e in entries
    ]
    bm25 = BM25Okapi(tokenized_corpus)
    logger.success(f"BM25 index built. Vocabulary size: {len(bm25.idf)}")

    # 3. 测试查询
    logger.info("\n--- BM25 检索效果测试 ---")
    for query in TEST_QUERIES:
        tokens = list(jieba.cut(query))
        scores = bm25.get_scores(tokens)
        top3 = sorted(enumerate(scores), key=lambda x: x[1], reverse=True)[:3]
        logger.info(f"\n查询: 「{query}」")
        for rank, (idx, score) in enumerate(top3, 1):
            if score > 0:
                e = entries[idx]
                logger.info(f"  {rank}. [{e['category']}] {e['title']} (score={score:.2f})")
            else:
                logger.info(f"  {rank}. (no match)")

    # 4. 统计各分类条目数
    logger.info("\n--- 知识库分类统计 ---")
    from collections import Counter
    category_counts = Counter(e.get("category", "UNKNOWN") for e in entries)
    for cat, count in sorted(category_counts.items()):
        logger.info(f"  {cat}: {count} 条")

    # 5. 危机条目检查
    crisis = [e for e in entries if e.get("risk_level_hint") in ("CRITICAL", "HIGH")]
    logger.info(f"\n危机/高风险条目: {len(crisis)} 条")
    for e in crisis:
        logger.info(f"  [{e['risk_level_hint']}] {e['title']}")

    logger.success("\n知识库验证完成，BM25 索引可正常工作")


if __name__ == "__main__":
    build_and_validate()
