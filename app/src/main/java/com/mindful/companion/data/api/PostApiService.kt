package com.mindful.companion.data.api

import retrofit2.Response
import retrofit2.http.*

interface PostApiService {
    
    @POST("posts")
    suspend fun createPost(
        @Header("Authorization") token: String,
        @Body request: CreatePostRequest
    ): Response<PostResponse>
    
    @GET("posts")
    suspend fun getPosts(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<List<PostResponse>>
    
    @POST("ai/analyze-emotion")
    suspend fun analyzeEmotion(
        @Header("Authorization") token: String,
        @Body request: EmotionAnalysisRequest
    ): Response<EmotionAnalysis>
}

// 请求和响应数据类
data class CreatePostRequest(
    val content: String,
    val emotion_data: EmotionData? = null,
    val risk_level: String? = null
)

data class EmotionData(
    val happiness: Float,
    val sadness: Float,
    val anger: Float,
    val fear: Float,
    val anxiety: Float,
    val overall: Float
)

data class PostResponse(
    val id: String,
    val content: String,
    val emotion_data: EmotionData?,
    val risk_level: String?,
    val ai_response: String?,
    val created_at: String
)

data class EmotionAnalysisRequest(
    val text: String
)

data class EmotionAnalysis(
    val happiness: Float,
    val sadness: Float,
    val anger: Float,
    val fear: Float,
    val anxiety: Float,
    val overall: Float,
    val riskKeywords: List<String>
)
