package com.mindful.companion.data.api

import retrofit2.Response
import retrofit2.http.*

interface AuthApiService {
    
    @GET("schools")
    suspend fun getSchools(): Response<SchoolsResponse>
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    
    @POST("auth/change-password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body request: ChangePasswordRequest
    ): Response<MessageResponse>
}

// 请求和响应数据类
data class SchoolsResponse(
    val schools: List<String>
)

data class LoginRequest(
    val school: String,
    val account: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val user_id: Int,
    val school: String,
    val account: String,
    val role: String = "user", // 默认为普通用户
    val message: String
)

data class ChangePasswordRequest(
    val old_password: String,
    val new_password: String
)

data class MessageResponse(
    val message: String
)
