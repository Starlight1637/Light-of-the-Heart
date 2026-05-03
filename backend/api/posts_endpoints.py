"""
帖子（日记）接口：发布 / 获取列表
同时处理 /ai/analyze-emotion / /ai/generate-response / /emergency/alert / /feedback
"""
from fastapi import APIRouter, Depends, Header, HTTPException
from pydantic import BaseModel
from sqlalchemy.orm import Session
from typing import Optional
from datetime import datetime
import re

from backend.database import get_db
from backend.models import Post, User, WatchListEntry, FlaggedEntry, Feedback
from backend.auth import get_current_user_id

router = APIRouter(tags=["Posts & AI"])

# ----------------------------------------------------------------
# 情绪关键词（离线分析）
# ----------------------------------------------------------------

CRISIS_KW = ["自杀", "结束生命", "不想活", "死了算了", "去死", "伤害自己", "割腕", "跳楼", "轻生"]
SAD_KW = ["悲伤", "难过", "哭", "痛苦", "心痛", "失落", "绝望", "崩溃"]
FEAR_KW = ["害怕", "恐惧", "担心", "紧张", "恐慌", "焦虑"]
ANGER_KW = ["愤怒", "生气", "发火", "烦躁", "讨厌", "愤恨"]
HAPPY_KW = ["开心", "高兴", "快乐", "幸福", "愉快", "喜悦", "满足"]


def analyze_emotion_offline(text: str) -> dict:
    lower = text
    score = lambda kws: min(1.0, sum(1 for k in kws if k in lower) * 0.25)
    sadness = score(SAD_KW)
    fear = score(FEAR_KW)
    anger = score(ANGER_KW)
    happiness = score(HAPPY_KW)
    anxiety = fear * 0.8
    overall = max(0.0, 1.0 - sadness * 0.5 - fear * 0.3 - anger * 0.2)

    is_crisis = any(k in text for k in CRISIS_KW)
    has_high = sadness > 0.5 or fear > 0.5 or any(k in text for k in ["绝望", "崩溃", "撑不住"])

    risk_level = "CRITICAL" if is_crisis else ("HIGH" if has_high else ("MEDIUM" if sadness > 0.25 else "LOW"))
    return {
        "happiness": happiness, "sadness": sadness,
        "anger": anger, "fear": fear, "anxiety": anxiety,
        "overall": overall, "riskKeywords": [],
        "risk_level": risk_level
    }


# ----------------------------------------------------------------
# 模型
# ----------------------------------------------------------------

class CreatePostRequest(BaseModel):
    content: str
    emotion_data: Optional[dict] = None
    risk_level: Optional[str] = None


class PostResponse(BaseModel):
    id: str
    content: str
    emotion_data: Optional[dict] = None
    risk_level: str = "LOW"
    ai_response: Optional[str] = None
    created_at: str
    author_id: int = 0
    author_name: str = "匿名用户"
    like_count: int = 0
    comment_count: int = 0


class EmotionAnalysisRequest(BaseModel):
    text: str


class AIResponseRequest(BaseModel):
    postContent: str
    emotionAnalysis: Optional[dict] = None
    riskLevel: str = "LOW"


class AIResponse(BaseModel):
    message: str
    sources: list[str] = []


class EmergencyAlert(BaseModel):
    postId: Optional[str] = None
    riskLevel: str
    content: str
    timestamp: Optional[str] = None


class FeedbackSubmitRequest(BaseModel):
    id: Optional[str] = None
    content: str


class MessageResponse(BaseModel):
    message: str


def _emotion_payload(score: Optional[float]) -> Optional[dict]:
    if score is None:
        return None
    normalized = max(0.0, min(1.0, float(score)))
    return {
        "happiness": normalized,
        "sadness": max(0.0, 1.0 - normalized),
        "anger": 0.0,
        "fear": 0.0,
        "anxiety": 0.0,
        "overall": normalized,
        "riskKeywords": [],
    }


# ----------------------------------------------------------------
# 路由
# ----------------------------------------------------------------

@router.post("/posts", response_model=PostResponse)
def create_post(
    req: CreatePostRequest,
    authorization: Optional[str] = Header(default=None),
    db: Session = Depends(get_db)
):
    user_id = get_current_user_id(authorization)
    emotion = req.emotion_data or analyze_emotion_offline(req.content)
    risk = req.risk_level or emotion.get("risk_level", "LOW")

    import uuid
    post_id = str(uuid.uuid4())
    post = Post(
        id=post_id,
        user_id=user_id,
        content=req.content,
        emotion_score=emotion.get("overall"),
        risk_level=risk,
        created_at=datetime.utcnow()
    )
    db.add(post)

    # 高风险：加入监控名单
    if risk in ("HIGH", "CRITICAL"):
        _add_to_watchlist(db, user_id, post_id, risk, req.content)

    db.commit()
    return PostResponse(
        id=post.id,
        content=post.content,
        emotion_data=emotion,
        risk_level=post.risk_level,
        created_at=post.created_at.isoformat(),
        author_id=user_id
    )


@router.get("/posts", response_model=list[PostResponse])
def get_posts(
    page: int = 0,
    size: int = 20,
    authorization: Optional[str] = Header(default=None),
    db: Session = Depends(get_db)
):
    user_id = get_current_user_id(authorization)
    posts = db.query(Post).filter(Post.user_id == user_id)\
        .order_by(Post.created_at.desc())\
        .offset(page * size)\
        .limit(size)\
        .all()
    return [PostResponse(
        id=p.id,
        content=p.content,
        emotion_data=_emotion_payload(p.emotion_score),
        risk_level=p.risk_level,
        ai_response=p.ai_response,
        created_at=p.created_at.isoformat(),
        author_id=p.user_id,
        like_count=p.like_count,
        comment_count=p.comment_count
    ) for p in posts]


@router.post("/ai/analyze-emotion")
def analyze_emotion(req: EmotionAnalysisRequest):
    return analyze_emotion_offline(req.text)


@router.post("/ai/generate-response", response_model=AIResponse)
def generate_ai_response(req: AIResponseRequest):
    risk = req.riskLevel
    if risk == "CRITICAL":
        msg = "我看到你正在经历非常困难的时刻。你现在的感受我都听到了，请立刻拨打心理援助热线 12356 或 010-82951332，24小时都有人接听。你不是一个人在面对这些。"
    elif risk == "HIGH":
        msg = "听到你说的这些，我很担心你。你现在的感受是真实的，也很重要。如果你感到难以承受，可以拨打心理援助热线 12356，随时有人陪你。"
    else:
        msg = "谢谢你愿意把这些分享出来。写下来本身就是很有勇气的事。如果你想聊聊，我一直在这里。"
    return AIResponse(message=msg)


@router.post("/emergency/alert", response_model=MessageResponse)
def emergency_alert(
    alert: EmergencyAlert,
    authorization: Optional[str] = Header(default=None),
    db: Session = Depends(get_db)
):
    user_id = get_current_user_id(authorization)
    _add_to_watchlist(db, user_id, alert.postId, alert.riskLevel, alert.content)
    db.commit()
    return MessageResponse(message="已收到紧急求助，心理顾问将尽快联系你")


@router.post("/feedback", response_model=MessageResponse)
def submit_feedback(
    req: FeedbackSubmitRequest,
    authorization: Optional[str] = Header(default=None),
    db: Session = Depends(get_db)
):
    user_id = get_current_user_id(authorization)
    fb = Feedback(user_id=user_id, content=req.content)
    db.add(fb)
    db.commit()
    return MessageResponse(message="反馈已提交，感谢你的意见")


# ----------------------------------------------------------------
# 内部工具
# ----------------------------------------------------------------

# ----------------------------------------------------------------
# 周报
# ----------------------------------------------------------------

class DailyMoodData(BaseModel):
    date: str
    avg_score: float
    post_count: int
    risk_events: int


class MoodDistribution(BaseModel):
    positive: float
    neutral: float
    negative: float


class WeeklyReportResponse(BaseModel):
    week: str
    daily_moods: list[DailyMoodData]
    dominant_mood: str
    mood_distribution: MoodDistribution
    ai_summary: str
    total_posts: int


def _iso_week_range(week_str: str):
    """Parse '2026-W12' → (week_start datetime, week_end datetime)."""
    from datetime import datetime, timedelta
    import re
    m = re.match(r"(\d{4})-W(\d{1,2})$", week_str or "")
    if not m:
        today = datetime.utcnow()
        week_start = today - timedelta(days=today.weekday())
    else:
        year, week = int(m.group(1)), int(m.group(2))
        jan4 = datetime(year, 1, 4)
        week_start = jan4 + timedelta(weeks=week - 1, days=-jan4.weekday())
    week_end = week_start + timedelta(days=7)
    return week_start.replace(hour=0, minute=0, second=0, microsecond=0), week_end


@router.get("/profile/weekly-report", response_model=WeeklyReportResponse)
def get_weekly_report(
    week: Optional[str] = None,
    authorization: Optional[str] = Header(default=None),
    db: Session = Depends(get_db)
):
    from datetime import timedelta
    user_id = get_current_user_id(authorization)
    week_start, week_end = _iso_week_range(week)

    posts = db.query(Post).filter(
        Post.user_id == user_id,
        Post.created_at >= week_start,
        Post.created_at < week_end
    ).all()

    # Build 7-day structure
    daily: list[DailyMoodData] = []
    for i in range(7):
        day = week_start + timedelta(days=i)
        day_next = day + timedelta(days=1)
        day_posts = [p for p in posts if day <= p.created_at < day_next]
        scores = [p.emotion_score if p.emotion_score is not None else 0.5 for p in day_posts]
        avg = sum(scores) / len(scores) if scores else 0.0
        risk_events = sum(1 for p in day_posts if p.risk_level in ("HIGH", "CRITICAL"))
        daily.append(DailyMoodData(
            date=day.strftime("%Y-%m-%d"),
            avg_score=round(avg, 3),
            post_count=len(day_posts),
            risk_events=risk_events
        ))

    total = len(posts)
    all_scores = [p.emotion_score if p.emotion_score is not None else 0.5 for p in posts]
    overall_avg = sum(all_scores) / len(all_scores) if all_scores else 0.5

    # Dominant mood
    if overall_avg >= 0.7:
        dominant = "愉悦"
    elif overall_avg >= 0.5:
        dominant = "平静"
    elif overall_avg >= 0.3:
        dominant = "低落"
    else:
        dominant = "困扰"

    # Mood distribution
    pos = sum(1 for s in all_scores if s >= 0.6)
    neu = sum(1 for s in all_scores if 0.35 <= s < 0.6)
    neg = sum(1 for s in all_scores if s < 0.35)
    n = len(all_scores) or 1
    dist = MoodDistribution(
        positive=round(pos / n, 3),
        neutral=round(neu / n, 3),
        negative=round(neg / n, 3)
    )

    # Static AI summary
    if total == 0:
        summary = "本周暂无记录。建议每天写一篇心情日记，帮助自己觉察情绪变化。"
    elif overall_avg >= 0.65:
        summary = f"本周你共记录了 {total} 篇日记，整体情绪状态良好。继续保持规律的情绪记录习惯，你做得很棒！"
    elif overall_avg >= 0.45:
        summary = f"本周你共记录了 {total} 篇日记，情绪整体较为平稳。偶有起伏是正常的，继续关注自己的感受。"
    else:
        summary = f"本周你共记录了 {total} 篇日记，情绪有些低落。这很正常，请记得使用呼吸练习或联系心理支持资源。"

    week_label = week or week_start.strftime("%Y-W%V")
    return WeeklyReportResponse(
        week=week_label,
        daily_moods=daily,
        dominant_mood=dominant,
        mood_distribution=dist,
        ai_summary=summary,
        total_posts=total
    )


def _add_to_watchlist(db, user_id: int, post_id: Optional[str], risk: str, content: str):
    entry = db.query(WatchListEntry).filter(WatchListEntry.user_id == user_id).first()
    if entry:
        entry.latest_risk_level = risk
        entry.is_handled = False
    else:
        db.add(WatchListEntry(user_id=user_id, latest_risk_level=risk))

    db.add(FlaggedEntry(
        user_id=user_id,
        post_id=post_id,
        risk_level=risk,
        content_preview=content[:300]
    ))
