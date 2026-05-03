"""
Self-RAG Judge: 决策是否需要检索，以及评估生成质量
使用 DeepSeek API（OpenAI 兼容格式）
"""
from enum import Enum
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


class RetrievalNeed(Enum):
    REQUIRED = "required"
    OPTIONAL = "optional"
    NOT_NEEDED = "not_needed"


class RiskLevel(Enum):
    LOW = "LOW"
    MEDIUM = "MEDIUM"
    HIGH = "HIGH"
    CRITICAL = "CRITICAL"


@dataclass
class JudgeResult:
    need_retrieval: RetrievalNeed
    risk_level: RiskLevel
    reasoning: str
    rewritten_query: Optional[str] = None


CRISIS_KEYWORDS = [
    "自杀", "结束生命", "不想活", "死了算了", "去死",
    "伤害自己", "自伤", "割腕", "跳楼", "轻生"
]

RISK_HIGH_KEYWORDS = [
    "绝望", "活不下去", "没有意义", "不想继续", "消失",
    "麻木", "崩溃", "撑不住了"
]

SIMPLE_QUERY_PATTERNS = [
    r"^(谢谢|好的|嗯|哦|明白了|知道了|好).*$",
    r"^.{1,4}$",
]


class SelfRAGJudge:

    def __init__(self):
        self.client = get_client()

    def assess_retrieval_need(self, user_message: str) -> JudgeResult:
        risk_level, is_crisis = self._hard_rule_check(user_message)

        if is_crisis:
            return JudgeResult(
                need_retrieval=RetrievalNeed.REQUIRED,
                risk_level=RiskLevel.CRITICAL,
                reasoning="检测到危机关键词，强制检索危机干预协议",
                rewritten_query=f"危机干预 自杀预防 {user_message[:50]}"
            )

        if self._is_simple_message(user_message):
            return JudgeResult(
                need_retrieval=RetrievalNeed.NOT_NEEDED,
                risk_level=risk_level,
                reasoning="简单消息，无需检索"
            )

        return self._llm_assess(user_message, risk_level)

    def _hard_rule_check(self, text: str) -> tuple:
        for kw in CRISIS_KEYWORDS:
            if kw in text:
                return RiskLevel.CRITICAL, True
        for kw in RISK_HIGH_KEYWORDS:
            if kw in text:
                return RiskLevel.HIGH, False
        return RiskLevel.LOW, False

    def _is_simple_message(self, text: str) -> bool:
        for pattern in SIMPLE_QUERY_PATTERNS:
            if re.match(pattern, text.strip()):
                return True
        return False

    def _llm_assess(self, user_message: str, initial_risk: RiskLevel) -> JudgeResult:
        try:
            prompt = f"""分析以下用户消息，判断：
1. 是否需要从心理健康知识库检索知识来支持回复？
2. 用户的情绪风险等级（LOW/MEDIUM/HIGH/CRITICAL）
3. 如需检索，改写为更适合检索的查询

用户消息："{user_message}"

JSON格式回复：
{{"need_retrieval":"required/optional/not_needed","risk_level":"LOW/MEDIUM/HIGH/CRITICAL","reasoning":"简短理由","rewritten_query":"检索查询或null"}}"""

            resp = self.client.chat.completions.create(
                model="deepseek-chat",
                messages=[{"role": "user", "content": prompt}],
                temperature=0.1,
                max_tokens=200
            )
            content = resp.choices[0].message.content
            m = re.search(r'\{.*\}', content, re.DOTALL)
            if m:
                data = json.loads(m.group())
                return JudgeResult(
                    need_retrieval=RetrievalNeed(data.get("need_retrieval", "optional")),
                    risk_level=RiskLevel(data.get("risk_level", initial_risk.value)),
                    reasoning=data.get("reasoning", ""),
                    rewritten_query=data.get("rewritten_query")
                )
        except Exception:
            pass
        return JudgeResult(
            need_retrieval=RetrievalNeed.OPTIONAL,
            risk_level=initial_risk,
            reasoning="LLM判断失败，降级"
        )

    def evaluate_generation_quality(self, generated_text: str, context: str) -> float:
        try:
            prompt = f"""评估以下AI心理支持回复质量（0.0-1.0）：
参考知识：{context[:400]}
生成回复：{generated_text[:400]}
评分标准：基于知识40%+语言温暖30%+具体建议20%+引导专业帮助10%
只回复一个0.0到1.0的数字。"""
            resp = self.client.chat.completions.create(
                model="deepseek-chat",
                messages=[{"role": "user", "content": prompt}],
                temperature=0.0,
                max_tokens=10
            )
            s = resp.choices[0].message.content.strip()
            return float(re.search(r'\d+\.?\d*', s).group())
        except Exception:
            return 0.7
