package com.mindful.companion.data.model

import java.util.Date

data class ChatSession(
    val sessionId: String,
    val userId: Int,
    val startTime: Date,
    val endTime: Date? = null,
    val moodReport: String? = null,
    val isActive: Boolean = true
) {
    val createdAt: Date
        get() = startTime
}
