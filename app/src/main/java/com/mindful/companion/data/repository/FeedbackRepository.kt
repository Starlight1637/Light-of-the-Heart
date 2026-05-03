package com.mindful.companion.data.repository

import com.mindful.companion.data.api.FeedbackSubmitRequest
import com.mindful.companion.data.api.MindfulApiService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for student feedback operations
 * Requirements: 5.4
 */
@Singleton
class FeedbackRepository @Inject constructor(
    private val mindfulApiService: MindfulApiService,
    private val authRepository: AuthRepository
) {
    
    private fun getAuthToken(): String {
        return "Bearer ${authRepository.getToken() ?: ""}"
    }
    
    /**
     * Submit user feedback to admin
     * Requirements: 5.4
     */
    suspend fun submitFeedback(id: String, content: String): Result<String> {
        return try {
            val request = FeedbackSubmitRequest(id, content)
            val response = mindfulApiService.submitFeedback(getAuthToken(), request)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.message)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "登录已过期，请重新登录"
                    400 -> "反馈内容不能为空"
                    else -> "提交失败，请稍后重试"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("网络连接失败"))
        }
    }
}
