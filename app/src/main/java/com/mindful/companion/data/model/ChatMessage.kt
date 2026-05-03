package com.mindful.companion.data.model

import java.util.Date

data class ChatMessage(
    val id: String,
    val userId: Int,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Date,
    val sessionId: String // 用于标识一次完整的对话会话
)
