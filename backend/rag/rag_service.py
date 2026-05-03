"""
MindfulRAGService: 整合 Self-RAG + CRAG 的主服务
使用 BM25（rank_bm25 + jieba）替代向量检索，无需 GPU 或嵌入 API
"""
import os
import json
from pathlib import Path
from loguru import logger

import jieba
from rank_bm25 import BM25Okapi

from .self_rag_judge import SelfRAGJudge, RetrievalNeed
from .crag_evaluator import CRAGEvaluator
from .self_reflective_generator import SelfRAGGenerator


class MindfulRAGService:

    def __init__(self):
        self.judge = SelfRAGJudge()
        self.evaluator = CRAGEvaluator()
        self.generator = SelfRAGGenerator(num_candidates=2)

        # 加载知识库并构建 BM25 索引
        kb_path = self._resolve_knowledge_base_path()
        self.knowledge_base: list[dict] = []
        self.bm25: BM25Okapi | None = None
        self._load_knowledge_base(str(kb_path))

    def _resolve_knowledge_base_path(self) -> Path:
        project_root = Path(__file__).resolve().parents[2]
        configured_path = os.getenv("KNOWLEDGE_BASE_PATH")
        if configured_path:
            path = Path(configured_path)
            return path if path.is_absolute() else project_root / path
        return project_root / "knowledge" / "knowledge_base.json"

    def _load_knowledge_base(self, kb_path: str) -> None:
        try:
            with open(kb_path, "r", encoding="utf-8") as f:
                self.knowledge_base = json.load(f)

            # 为每条知识条目构建分词文本（title + keywords + content）
            tokenized_corpus = [
                list(jieba.cut(self._document_text(e)))
                for e in self.knowledge_base
            ]
            self.bm25 = BM25Okapi(tokenized_corpus)
            logger.info(f"BM25 index built with {len(self.knowledge_base)} entries from {kb_path}")
        except FileNotFoundError:
            logger.warning(f"Knowledge base not found at {kb_path}. Run build_knowledge_base.py first.")
        except Exception as e:
            logger.error(f"Failed to load knowledge base: {e}")

    # ----------------------------------------------------------------
    # 检索入口
    # ----------------------------------------------------------------

    def retrieve_knowledge(
        self,
        query: str,
        risk_level: str = "LOW",
        top_k: int = 5
    ) -> list[dict]:
        """CRAG + Self-RAG 混合检索"""

        # 1. Self-RAG Judge: 是否需要检索
        judge_result = self.judge.assess_retrieval_need(query)
        logger.info(f"Judge result: need={judge_result.need_retrieval}, risk={judge_result.risk_level}")

        # 覆盖传入的 risk_level（以 Judge 检测为准）
        effective_risk = judge_result.risk_level.value

        if judge_result.need_retrieval == RetrievalNeed.NOT_NEEDED:
            return []

        # 2. BM25 检索
        search_query = judge_result.rewritten_query or query
        raw_docs = self._bm25_search(search_query, top_k=top_k * 2)

        # 3. CRAG 评估与修正
        eval_result = self.evaluator.evaluate_retrieved_docs(query, raw_docs, effective_risk)

        if eval_result.strategy == "corrective_retrieval" and eval_result.corrective_query:
            corrective_docs = self._bm25_search(eval_result.corrective_query, top_k=top_k)
            raw_docs = corrective_docs + (eval_result.refined_docs or [])

        # 危机场景：强制追加危机资源
        if effective_risk in ("HIGH", "CRITICAL"):
            crisis_docs = self._get_crisis_docs()
            raw_docs = self._merge_docs(raw_docs, crisis_docs, top_k)
        else:
            raw_docs = raw_docs[:top_k]

        return raw_docs

    # ----------------------------------------------------------------
    # 生成入口
    # ----------------------------------------------------------------

    def generate_response(
        self,
        user_message: str,
        risk_level: str,
        retrieved_docs: list[dict],
        session_history: list[dict] | None = None
    ) -> dict:
        """Self-RAG 自我反思生成"""

        response_text = self.generator.generate_with_self_reflection(
            user_message=user_message,
            retrieved_docs=retrieved_docs,
            risk_level=risk_level,
            session_history=session_history
        )

        sources_used = [doc.get("title", "") for doc in retrieved_docs[:3]]
        confidence = self.judge.evaluate_generation_quality(
            response_text,
            context="\n".join(d.get("content", "") for d in retrieved_docs[:2])
        )

        return {
            "message": response_text,
            "sources_used": sources_used,
            "confidence": confidence,
            "self_rag_passed": confidence >= 0.6
        }

    # ----------------------------------------------------------------
    # 内部工具
    # ----------------------------------------------------------------

    def _document_text(self, entry: dict) -> str:
        title = entry.get("title", "")
        keywords = entry.get("keywords", "")
        category = entry.get("category", "")
        content = entry.get("content", "")
        return f"{title} {title} {keywords} {keywords} {keywords} {category} {content}"

    def _bm25_search(self, query: str, top_k: int = 5) -> list[dict]:
        """BM25 关键词检索"""
        if not self.bm25 or not self.knowledge_base:
            logger.warning("BM25 index not available. Run build_knowledge_base.py first.")
            return []
        try:
            tokens = list(jieba.cut(query))
            scores = self.bm25.get_scores(tokens)
            # 取 top_k 条非零得分文档
            indexed = sorted(enumerate(scores), key=lambda x: x[1], reverse=True)[:top_k]
            results = []
            for idx, score in indexed:
                if score <= 0:
                    continue
                doc = self.knowledge_base[idx]
                results.append({
                    **doc,
                    "relevance_score": round(min(1.0, score / 10.0), 3)
                })
            return results
        except Exception as e:
            logger.error(f"BM25 search error: {e}")
            return []

    def _get_crisis_docs(self) -> list[dict]:
        """获取危机干预专用文档"""
        return [
            doc for doc in self.knowledge_base
            if doc.get("risk_level_hint") in ("CRITICAL", "HIGH")
        ][:3]

    def _merge_docs(self, primary: list[dict], secondary: list[dict], top_k: int) -> list[dict]:
        seen: set[str] = set()
        merged: list[dict] = []
        for doc in primary + secondary:
            key = str(doc.get("id", doc.get("title", "")))[:20]
            if key not in seen:
                seen.add(key)
                merged.append(doc)
                if len(merged) >= top_k:
                    break
        return merged
