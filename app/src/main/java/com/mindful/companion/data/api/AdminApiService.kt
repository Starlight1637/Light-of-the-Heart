package com.mindful.companion.data.api

import com.mindful.companion.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Admin API Service for admin-specific endpoints
 * Provides interfaces for watch list, post management, reports, account creation, and feedback
 */
interface AdminApiService {
    
    // ==================== Watch List Endpoints ====================
    // Requirements: 2.2, 2.3, 2.4
    
    /**
     * Get the list of students flagged for risk
     * Requirements: 2.2
     */
    @GET("admin/watchlist")
    suspend fun getWatchList(
        @Header("Authorization") token: String
    ): Response<List<WatchListItem>>
    
    /**
     * Get flagged diary entries for a specific user
     * Requirements: 2.3
     */
    @GET("admin/watchlist/{userId}/entries")
    suspend fun getFlaggedEntries(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int
    ): Response<List<FlaggedEntry>>
    
    /**
     * Mark a student as handled in the watch list
     * Requirements: 2.4
     */
    @PUT("admin/watchlist/{userId}/handle")
    suspend fun markAsHandled(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int
    ): Response<MessageResponse>
    
    // ==================== Report Management Endpoints ====================
    // Requirements: 4.2, 4.4
    
    /**
     * Get all reports sent to admin by students
     * Requirements: 4.2
     */
    @GET("admin/reports")
    suspend fun getAdminReports(
        @Header("Authorization") token: String
    ): Response<List<AdminReport>>

    @GET("admin/reports/{reportId}")
    suspend fun getReportDetail(
        @Header("Authorization") token: String,
        @Path("reportId") reportId: String
    ): Response<AdminReport>
    
    /**
     * Mark a report as reviewed
     * Requirements: 4.4
     */
    @PUT("admin/reports/{reportId}/review")
    suspend fun markReportReviewed(
        @Header("Authorization") token: String,
        @Path("reportId") reportId: String
    ): Response<MessageResponse>
    
    // ==================== Account Management Endpoints ====================
    // Requirements: 6.4
    
    /**
     * Create multiple accounts in batch
     * Requirements: 6.4
     */
    @POST("admin/accounts/batch")
    suspend fun createBatchAccounts(
        @Header("Authorization") token: String,
        @Body request: BatchAccountRequest
    ): Response<BatchAccountResponse>
    
    // ==================== Feedback Endpoints ====================
    // Requirements: 5.4
    
    /**
     * Get all user feedback submissions
     * Requirements: 5.4
     */
    @GET("admin/feedback")
    suspend fun getFeedback(
        @Header("Authorization") token: String
    ): Response<List<FeedbackItem>>
}
