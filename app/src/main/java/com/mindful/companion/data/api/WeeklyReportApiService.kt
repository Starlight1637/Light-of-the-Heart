package com.mindful.companion.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface WeeklyReportApiService {
    @GET("profile/weekly-report")
    suspend fun getWeeklyReport(
        @Header("Authorization") token: String,
        @Query("week") week: String? = null
    ): Response<WeeklyReportResponse>
}

data class WeeklyReportResponse(
    val week: String,
    val daily_moods: List<DailyMoodData>,
    val dominant_mood: String,
    val mood_distribution: MoodDistribution,
    val ai_summary: String,
    val total_posts: Int
)

data class DailyMoodData(
    val date: String,
    val avg_score: Float,
    val post_count: Int,
    val risk_events: Int
)

data class MoodDistribution(
    val positive: Float,
    val neutral: Float,
    val negative: Float
)
