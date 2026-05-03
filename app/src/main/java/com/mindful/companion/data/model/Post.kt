package com.mindful.companion.data.model

import java.util.Date

data class Post(
    val id: String,
    val content: String,
    val audioPath: String? = null,
    val imagePath: String? = null,
    val timestamp: Date,
    val isAnonymous: Boolean = true,
    val emotionScore: Float? = null,
    val riskLevel: RiskLevel = RiskLevel.LOW,
    val aiResponse: String? = null,
    val authorId: Int = 0,
    val authorName: String = "匿名用户"
)

enum class RiskLevel {
    LOW, MEDIUM, HIGH, CRITICAL
}

data class EmotionAnalysis(
    val happiness: Float,
    val sadness: Float,
    val anger: Float,
    val fear: Float,
    val anxiety: Float,
    val overall: Float,
    val riskKeywords: List<String> = emptyList()
)
