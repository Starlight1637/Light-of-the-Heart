package com.mindful.companion.data.model

import com.google.gson.annotations.SerializedName

data class WatchListItem(
    @SerializedName("userId")
    val userId: Int,
    val account: String,
    val school: String,
    val flaggedAt: String,
    val riskLevel: String,
    val status: String,
    val latestKeywords: List<String> = emptyList()
)

data class FlaggedEntry(
    @SerializedName("entryId")
    val entryId: String,
    val content: String,
    val riskKeywords: List<String> = emptyList(),
    val timestamp: String
)

data class AdminReport(
    val id: String,
    @SerializedName("userId")
    val userId: Int,
    val account: String,
    val school: String,
    val reportDate: String,
    val moodSummary: String,
    val riskIndicators: List<String> = emptyList(),
    val isReviewed: Boolean
)

data class BatchAccountRequest(
    val school: String,
    val accountStart: String,
    val accountEnd: String,
    val role: String = "user"
)

data class BatchAccountResponse(
    val totalRequested: Int,
    val successCount: Int,
    val failedCount: Int,
    val skippedAccounts: List<String> = emptyList(),
    val message: String
)

data class FeedbackItem(
    val id: String,
    @SerializedName("userId")
    val userId: Int,
    val account: String,
    val content: String,
    val createdAt: String
)
