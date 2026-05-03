"""
CRAG Evaluator: 评估检索文档质量，并在质量不足时进行修正检索
使用 DeepSeek API（OpenAI 兼容格式）
"""
from dataclasses import dataclass
from typing import Optional
from openai import OpenAI
import os
import re
import json


def get_client() -> OpenAI:
    return OpenAI(
        api_key=os.getenv("DEEPSEEK_API_KEY"),
        base_url="https://api.deepseek.com/v1"
    )


@dataclass
class DocumentScore:
    doc_id: str
    relevance: float       # 0-1, 与查询的相关性
    factual_quality: float # 0-1, 内容的事实准确性
    combined: float        # 综合得分
    action: str            # "use" / "refine" / "discard"


@dataclass
class EvaluationResult:
    scored_docs: list[DocumentScore]
    strategy: str           # "use_retrieved" / "corrective_retrieval"
    corrective_query: Optional[str] = None
    refined_docs: Optional[list[dict]] = None


RELEVANCE_THRESHOLD = 0.5
QUALITY_THRESHOLD = 0.4


class CRAGEvaluator:

    def __init__(self):
        self.client = get_client()

    def evaluate_retrieved_docs(
        self,
        query: str,
        retrieved_docs: list[dict],
        risk_level: str = "LOW"
    ) -> EvaluationResult:
        """
        评估检索到的文档集合质量

        返回策略：
        - use_retrieved: 文档质量足够，直接使用
        - corrective_retrieval: 文档质量不足，需要修正检索
        """
        if not retrieved_docs:
            return EvaluationResult(
                scored_docs=[],
                strategy="corrective_retrieval",
                corrective_query=self._rewrite_query(query)
            )

        scored = []
        for doc in retrieved_docs:
            score = self._score_document(query, doc)
            scored.append(score)

        # 高风险场景：降低接受阈值（确保危机资源总是被包含）
        threshold = RELEVANCE_THRESHOLD * 0.7 if risk_level in ("HIGH", "CRITICAL") else RELEVANCE_THRESHOLD

        usable = [s for s in scored if s.combined >= threshold]

        if len(usable) >= 2:
            return EvaluationResult(
                scored_docs=scored,
                strategy="use_retrieved"
            )
        else:
            corrective_query = self._rewrite_query(query)
            refined = self._refine_knowledge(
                query,
                [d for d, s in zip(retrieved_docs, scored) if s.action == "refine"]
            )
            return EvaluationResult(
                scored_docs=scored,
                strategy="corrective_retrieval",
                corrective_query=corrective_query,
                refined_docs=refined
            )

    def _score_document(self, query: str, doc: dict) -> DocumentScore:
        """LLM评分单篇文档"""
        try:
            prompt = f"""评估以下心理健康知识条目与查询的相关性和质量：

查询：{query}
知识条目标题：{doc.get('title', '')}
知识条目内容：{doc.get('content', '')[:400]}

请以JSON格式回复：
{{
    "relevance": 0.0-1.0,
    "factual_quality": 0.0-1.0,
    "action": "use/refine/discard"
}}"""

            response = self.client.chat.completions.create(
                model="deepseek-chat",
                messages=[{"role": "user", "content": prompt}],
                temperature=0.0,
                max_tokens=100
            )
            content = response.choices[0].message.content
            json_match = re.search(r'\{.*\}', content, re.DOTALL)
            if json_match:
                data = json.loads(json_match.group())
                relevance = float(data.get("relevance", 0.5))
                factual = float(data.get("factual_quality", 0.7))
                combined = relevance * 0.7 + factual * 0.3
                return DocumentScore(
                    doc_id=doc.get("id", ""),
                    relevance=relevance,
                    factual_quality=factual,
                    combined=combined,
                    action=data.get("action", "use")
                )
        except Exception:
            pass

        # 降级：给一个中等分数
        return DocumentScore(
            doc_id=doc.get("id", ""),
            relevance=0.5,
            factual_quality=0.7,
            combined=0.57,
            action="use"
        )

    def _rewrite_query(self, original_query: str) -> str:
        """重写查询以提高检索质量"""
        try:
            prompt = f"""将以下用户消息改写为更适合心理健康知识库检索的查询语句，
提取核心心理健康相关概念，去除个人描述性语言。

用户消息：{original_query}

只返回改写后的查询，不要其他解释。"""

            response = self.client.chat.completions.create(
                model="deepseek-chat",
                messages=[{"role": "user", "content": prompt}],
                temperature=0.2,
                max_tokens=50
            )
            return response.choices[0].message.content.strip()
        except Exception:
            keywords = ["情绪", "焦虑", "抑郁", "压力", "睡眠", "人际关系"]
            for kw in keywords:
                if kw in original_query:
                    return f"{kw} 应对策略 心理技巧"
            return "心理健康 情绪调节"

    def _refine_knowledge(self, query: str, ambiguous_docs: list[dict]) -> list[dict]:
        """处理质量不确定的文档：提取并强化有用部分"""
        if not ambiguous_docs:
            return []
        refined = []
        for doc in ambiguous_docs[:2]:
            try:
                prompt = f"""从以下知识条目中提取与查询最相关的部分，去除不相关内容：

查询：{query}
知识条目：{doc.get('content', '')[:600]}

返回精炼后的相关内容（100-200字）："""

                response = self.client.chat.completions.create(
                    model="deepseek-chat",
                    messages=[{"role": "user", "content": prompt}],
                    temperature=0.3,
                    max_tokens=300
                )
                refined.append({
                    **doc,
                    "content": response.choices[0].message.content.strip(),
                    "refined": True
                })
            except Exception:
                refined.append(doc)
        return refined
