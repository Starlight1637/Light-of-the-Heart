"""
Self-Reflective Generator: 生成多候选回复并通过自我评估选出最佳回复
使用 DeepSeek API（OpenAI 兼容格式）
"""
from dataclasses import dataclass
from openai import OpenAI
import os
import re
import json


def get_client() -> OpenAI:
    api_key = os.getenv("DEEPSEEK_API_KEY")
    if not api_key:
        raise RuntimeError("DEEPSEEK_API_KEY is not configured")
    return OpenAI(
        api_key=api_key,
        base_url="https://api.deepseek.com/v1"
    )


SYSTEM_PROMPT = """你是「心光」心理健康陪伴AI，专为大学生设计。

你的角色：
- 温暖、真诚的倾听者，不是治疗师
- 提供情感支持和心理健康知识，不替代专业治疗
- 语言自然、口语化，避免说教
- 在高风险情境下，必须提供专业求助资源

核心原则：
1. 先共情，再建议
2. 尊重当事人的感受和选择
3. 不评判，不淡化痛苦
4. 高风险情境强制包含热线信息"""


@dataclass
class Candidate:
    text: str
    score: float
    passed_critique: bool


class SelfRAGGenerator:

    def __init__(self, num_candidates: int = 2):
        self.client: OpenAI | None = None
        self.num_candidates = num_candidates

    def _client(self) -> OpenAI:
        if self.client is None:
            self.client = get_client()
        return self.client

    def generate_with_self_reflection(
        self,
        user_message: str,
        retrieved_docs: list[dict],
        risk_level: str = "LOW",
        session_history: list[dict] | None = None
    ) -> str:
        """
        生成多个候选回复 -> 自我评估 -> 选出最佳
        """
        if not os.getenv("DEEPSEEK_API_KEY"):
            raise RuntimeError("DEEPSEEK_API_KEY is not configured")

        context = self._build_context(retrieved_docs, risk_level)
        candidates = []

        for i in range(self.num_candidates):
            temp = 0.7 if i == 0 else 0.9
            candidate_text = self._generate_candidate(
                user_message, context, risk_level, session_history, temperature=temp
            )
            score, passed = self._self_critique(candidate_text, user_message, context, risk_level)
            candidates.append(Candidate(text=candidate_text, score=score, passed_critique=passed))

        # 选出得分最高且通过评估的候选
        passed = [c for c in candidates if c.passed_critique]
        best = max(passed if passed else candidates, key=lambda c: c.score)

        # 高风险：强制检查热线信息
        result = best.text
        if risk_level in ("HIGH", "CRITICAL") and "400" not in result:
            result += "\n\n如果你感到很难熬，随时可以拨打心理援助热线 400-161-9995，24小时都有人接听。"

        return result

    def _build_context(self, docs: list[dict], risk_level: str) -> str:
        if not docs:
            return ""
        parts = ["以下是相关心理健康知识：\n"]
        for doc in docs[:4]:
            parts.append(f"【{doc.get('title', '')}】\n{doc.get('content', '')[:300]}\n")
        if risk_level in ("HIGH", "CRITICAL"):
            parts.append("\n危机资源：心理援助热线 400-161-9995（24小时），生命热线 400-821-1215")
        return "\n".join(parts)

    def _generate_candidate(
        self,
        user_message: str,
        context: str,
        risk_level: str,
        session_history: list[dict] | None,
        temperature: float
    ) -> str:
        messages = [{"role": "system", "content": SYSTEM_PROMPT}]

        if context:
            messages.append({
                "role": "system",
                "content": f"参考以下心理健康知识来支持你的回复：\n{context}"
            })

        if session_history:
            messages.extend(session_history[-4:])  # 最近2轮对话

        messages.append({"role": "user", "content": user_message})

        try:
            response = self._client().chat.completions.create(
                model="deepseek-chat",
                messages=messages,
                temperature=temperature,
                max_tokens=600
            )
            return response.choices[0].message.content
        except Exception:
            return "我在认真听你说的话。你愿意多告诉我一些吗？"

    def _self_critique(
        self,
        generated: str,
        original_query: str,
        context: str,
        risk_level: str
    ) -> tuple[float, bool]:
        """自我评估：检查回复是否符合质量标准"""
        try:
            prompt = f"""评估以下心理健康AI回复的质量：

用户消息：{original_query}
参考知识：{context[:300]}
AI回复：{generated[:400]}
风险等级：{risk_level}

评分标准（各项0-10分）：
1. 共情质量（是否真诚感受到用户的情绪）
2. 知识准确性（建议是否有心理学依据）
3. 安全性（高风险时是否提供专业资源）
4. 自然流畅（语言是否自然口语化）

以JSON格式回复：
{{
    "scores": {{
        "empathy": 0-10,
        "accuracy": 0-10,
        "safety": 0-10,
        "naturalness": 0-10
    }},
    "passed": true/false,
    "issues": "问题简述（如有）"
}}"""

            response = self._client().chat.completions.create(
                model="deepseek-chat",
                messages=[{"role": "user", "content": prompt}],
                temperature=0.0,
                max_tokens=200
            )
            content = response.choices[0].message.content
            json_match = re.search(r'\{.*\}', content, re.DOTALL)
            if json_match:
                data = json.loads(json_match.group())
                scores = data.get("scores", {})
                avg = sum(scores.values()) / len(scores) if scores else 7.0

                # 高风险场景：安全性权重加倍
                if risk_level in ("HIGH", "CRITICAL"):
                    safety = scores.get("safety", 7.0)
                    avg = (avg * len(scores) + safety) / (len(scores) + 1)

                return avg / 10.0, data.get("passed", avg >= 7.0)
        except Exception:
            pass

        return 0.7, True  # 降级默认
