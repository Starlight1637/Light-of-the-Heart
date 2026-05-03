package com.mindful.companion.data.api

import retrofit2.Response
import retrofit2.http.*

interface ZhipuAIService {

    @POST("api/chat/completions")
    suspend fun chatCompletions(
        @Header("Authorization") token: String,
        @Body request: ZhipuChatRequest
    ): Response<ZhipuChatResponse>
}

data class ZhipuChatRequest(
    val model: String = "deepseek-chat",
    val messages: List<ZhipuMessage>,
    val temperature: Float = 0.7f,
    val top_p: Float = 0.9f,
    val max_tokens: Int = 1024
)

data class ZhipuMessage(
    val role: String, // "system", "user", "assistant"
    val content: String
)

data class ZhipuChatResponse(
    val id: String,
    val created: Long,
    val model: String,
    val choices: List<ZhipuChoice>,
    val usage: ZhipuUsage?
)

data class ZhipuChoice(
    val index: Int,
    val message: ZhipuMessage,
    val finish_reason: String
)

data class ZhipuUsage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)
