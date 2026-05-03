package com.mindful.companion.data.repository

import com.mindful.companion.data.api.AdminApiService
import com.mindful.companion.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for admin-specific operations
 * Handles watch list, post management, reports, account creation, and feedback
 */
@Singleton
class AdminRepository @Inject constructor(
    private val adminApiService: AdminApiService,
    private val authRepository: AuthRepository
) {
    
    private fun getAuthToken(): String {
        return "Bearer ${authRepository.getToken() ?: ""}"
    }
    
    // ==================== Watch List Methods ====================
    // Requirements: 2.2, 2.3, 2.4
    
    /**
     * Get the list of students flagged for risk
     * Requirements: 2.2
     */
    suspend fun getWatchList(): Result<List<WatchListItem>> {
        return try {
            val response = adminApiService.getWatchList(getAuthToken())
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "登录已过期，请重新登录"
                    403 -> "需要管理员权限"
                    else -> "获取关注列表失败"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("网络连接失败"))
        }
    }
    
    /**
     * Get flagged diary entries for a specific user
     * Requirements: 2.3
     */
    suspend fun getFlaggedEntries(userId: Int): Result<List<FlaggedEntry>> {
        return try {
            val response = adminApiService.getFlaggedEntries(getAuthToken(), userId)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "登录已过期，请重新登录"
                    403 -> "需要管理员权限"
                    404 -> "用户不存在"
                    else -> "获取日记条目失败"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("网络连接失败"))
        }
    }
    
    /**
     * Mark a student as handled in the watch list
     * Requirements: 2.4
     */
    suspend fun markAsHandled(userId: Int): Result<String> {
        return try {
            val response = adminApiService.markAsHandled(getAuthToken(), userId)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.message)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "登录已过期，请重新登录"
                    403 -> "需要管理员权限"
                    404 -> "用户不存在"
                    else -> "标记失败"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("网络连接失败"))
        }
    }
    
    // ==================== Report Management Methods ====================
    // Requirements: 4.2, 4.4
    
    /**
     * Get all reports sent to admin by students
     * Requirements: 4.2
     */
    suspend fun getAdminReports(): Result<List<AdminReport>> {
        return try {
            val response = adminApiService.getAdminReports(getAuthToken())
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "登录已过期，请重新登录"
                    403 -> "需要管理员权限"
                    else -> "获取报告列表失败"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("网络连接失败"))
        }
    }

    suspend fun getReportDetail(reportId: String): Result<AdminReport> {
        return try {
            val response = adminApiService.getReportDetail(getAuthToken(), reportId)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "登录已过期，请重新登录"
                    403 -> "需要管理员权限"
                    404 -> "报告不存在"
                    else -> "获取报告详情失败"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("网络连接失败"))
        }
    }
    
    /**
     * Mark a report as reviewed
     * Requirements: 4.4
     */
    suspend fun markReportReviewed(reportId: String): Result<String> {
        return try {
            val response = adminApiService.markReportReviewed(getAuthToken(), reportId)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.message)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "登录已过期，请重新登录"
                    403 -> "需要管理员权限"
                    404 -> "报告不存在"
                    else -> "标记失败"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("网络连接失败"))
        }
    }
    
    // ==================== Account Management Methods ====================
    // Requirements: 6.4
    
    /**
     * Create multiple accounts in batch
     * Requirements: 6.4
     */
    suspend fun createBatchAccounts(request: BatchAccountRequest): Result<BatchAccountResponse> {
        return try {
            val response = adminApiService.createBatchAccounts(getAuthToken(), request)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = when (response.code()) {
                    400 -> "账号范围格式错误"
                    401 -> "登录已过期，请重新登录"
                    403 -> "需要管理员权限"
                    else -> "创建账号失败"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("网络连接失败"))
        }
    }
    
    // ==================== Feedback Methods ====================
    // Requirements: 5.4
    
    /**
     * Get all user feedback submissions
     * Requirements: 5.4
     */
    suspend fun getFeedback(): Result<List<FeedbackItem>> {
        return try {
            val response = adminApiService.getFeedback(getAuthToken())
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "登录已过期，请重新登录"
                    403 -> "需要管理员权限"
                    else -> "获取反馈列表失败"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("网络连接失败"))
        }
    }
}
