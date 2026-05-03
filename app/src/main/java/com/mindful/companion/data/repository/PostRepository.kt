package com.mindful.companion.data.repository

import com.mindful.companion.data.api.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepository @Inject constructor(
    private val postApiService: PostApiService,
    private val authRepository: AuthRepository
) {
    
    suspend fun createPost(
        content: String,
        emotionData: EmotionData? = null,
        riskLevel: String? = null
    ): Result<PostResponse> {
        return try {
            val token = authRepository.getToken() 
                ?: return Result.failure(Exception("未登录"))
            
            val request = CreatePostRequest(content, emotionData, riskLevel)
            val response = postApiService.createPost("Bearer $token", request)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "登录已过期，请重新登录"
                    else -> "发布失败，请稍后重试"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("网络连接失败: ${e.message}"))
        }
    }
    
    suspend fun getPosts(page: Int = 0, size: Int = 20): Result<List<PostResponse>> {
        return try {
            val token = authRepository.getToken() 
                ?: return Result.failure(Exception("未登录"))
            
            val response = postApiService.getPosts("Bearer $token", page, size)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "登录已过期，请重新登录"
                    else -> "获取失败，请稍后重试"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("网络连接失败: ${e.message}"))
        }
    }
    
    suspend fun analyzeEmotion(text: String): Result<EmotionAnalysis> {
        return try {
            val token = authRepository.getToken() 
                ?: return Result.failure(Exception("未登录"))
            
            val request = EmotionAnalysisRequest(text)
            val response = postApiService.analyzeEmotion("Bearer $token", request)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("分析失败"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("网络连接失败: ${e.message}"))
        }
    }
}
