package com.mindful.companion.data.api

import com.mindful.companion.data.model.AIResponse
import com.mindful.companion.data.model.RiskLevel
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface MindfulApiService {

    @POST("ai/analyze-emotion")
    suspend fun analyzeEmotion(
        @Body request: EmotionAnalysisRequest
    ): Response<com.mindful.companion.data.model.EmotionAnalysis>

    @POST("ai/generate-response")
    suspend fun generateAIResponse(@Body request: AIResponseRequest): Response<AIResponse>

    @POST("emergency/alert")
    suspend fun sendEmergencyAlert(
        @Header("Authorization") token: String,
        @Body alert: EmergencyAlert
    ): Response<Unit>

    @POST("feedback")
    suspend fun submitFeedback(
        @Header("Authorization") token: String,
        @Body request: FeedbackSubmitRequest
    ): Response<MessageResponse>
}

data class AIResponseRequest(
    val postContent: String,
    val emotionAnalysis: com.mindful.companion.data.model.EmotionAnalysis,
    val riskLevel: RiskLevel
)

data class EmergencyAlert(
    val postId: String,
    val riskLevel: RiskLevel,
    val content: String,
    val timestamp: Long
)

data class FeedbackSubmitRequest(
    val id: String,
    val content: String
)
