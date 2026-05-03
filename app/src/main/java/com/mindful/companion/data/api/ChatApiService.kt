package com.mindful.companion.data.api

import retrofit2.Response
import retrofit2.http.*

// 请求模型
data class ChatSessionCreateRequest(
    val session_id: String
)

data class ChatSessionUpdateRequest(
    val end_time: String? = null,
    val mood_report: String? = null,
    val is_active: Boolean? = null
)

data class ChatMessageCreateRequest(
    val id: String,
    val session_id: String,
    val content: String,
    val is_from_user: Boolean,
    val timestamp: String? = null
)

// 响应模型
data class ChatSessionResponse(
    val id: String,
    val user_id: Int,
    val start_time: String,
    val end_time: String?,
    val mood_report: String?,
    val is_active: Boolean
)

data class ChatMessageResponse(
    val id: String,
    val session_id: String,
    val user_id: Int,
    val content: String,
    val is_from_user: Boolean,
    val timestamp: String
)

interface ChatApiService {
    
    // 会话相关
    @POST("chat/sessions")
    suspend fun createSession(
        @Header("Authorization") token: String,
        @Body request: ChatSessionCreateRequest
    ): Response<ChatSessionResponse>
    
    @GET("chat/sessions")
    suspend fun getSessions(
        @Header("Authorization") token: String
    ): Response<List<ChatSessionResponse>>
    
    @GET("chat/sessions/active")
    suspend fun getActiveSession(
        @Header("Authorization") token: String
    ): Response<ChatSessionResponse?>
    
    @PUT("chat/sessions/{sessionId}")
    suspend fun updateSession(
        @Header("Authorization") token: String,
        @Path("sessionId") sessionId: String,
        @Body request: ChatSessionUpdateRequest
    ): Response<Map<String, String>>
    
    @DELETE("chat/sessions/{sessionId}")
    suspend fun deleteSession(
        @Header("Authorization") token: String,
        @Path("sessionId") sessionId: String
    ): Response<Map<String, String>>
    
    @POST("chat/sessions/deactivate-all")
    suspend fun deactivateAllSessions(
        @Header("Authorization") token: String
    ): Response<Map<String, String>>
    
    @PUT("chat/sessions/{sessionId}/send-to-admin")
    suspend fun sendSessionToAdmin(
        @Header("Authorization") token: String,
        @Path("sessionId") sessionId: String
    ): Response<Map<String, String>>
    
    // 消息相关
    @POST("chat/messages")
    suspend fun createMessage(
        @Header("Authorization") token: String,
        @Body request: ChatMessageCreateRequest
    ): Response<ChatMessageResponse>
    
    @GET("chat/sessions/{sessionId}/messages")
    suspend fun getSessionMessages(
        @Header("Authorization") token: String,
        @Path("sessionId") sessionId: String
    ): Response<List<ChatMessageResponse>>
}
