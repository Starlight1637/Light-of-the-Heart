package com.mindful.companion.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.mindful.companion.data.api.AuthApiService
import com.mindful.companion.data.api.ChangePasswordRequest
import com.mindful.companion.data.api.LoginRequest
import com.mindful.companion.data.api.LoginResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authApiService: AuthApiService
) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_SCHOOL = "school"
        private const val KEY_ACCOUNT = "account"
        private const val KEY_ROLE = "role"
    }

    suspend fun login(school: String, account: String, password: String): Result<LoginResponse> {
        return try {
            val request = LoginRequest(school, account, password)
            val response = authApiService.login(request)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "账号或密码错误"
                    404 -> "账号不存在"
                    else -> "登录失败，请稍后重试"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("网络连接失败"))
        }
    }

    suspend fun changePassword(oldPassword: String, newPassword: String): Result<String> {
        return try {
            val token = getToken() ?: return Result.failure(Exception("未登录"))
            val request = ChangePasswordRequest(oldPassword, newPassword)
            val response = authApiService.changePassword("Bearer $token", request)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.message)
            } else {
                val errorMsg = when (response.code()) {
                    400 -> "原密码错误"
                    401 -> "登录已过期，请重新登录"
                    else -> "修改失败，请稍后重试"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("网络连接失败"))
        }
    }

    fun saveLoginInfo(token: String, userId: Int, school: String, account: String, role: String = "user") {
        prefs.edit().apply {
            putString(KEY_TOKEN, token)
            putInt(KEY_USER_ID, userId)
            putString(KEY_SCHOOL, school)
            putString(KEY_ACCOUNT, account)
            putString(KEY_ROLE, role)
            apply()
        }
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, -1)

    fun getSchool(): String? = prefs.getString(KEY_SCHOOL, null)

    fun getAccount(): String? = prefs.getString(KEY_ACCOUNT, null)

    fun getRole(): String? = prefs.getString(KEY_ROLE, null)

    fun isAdmin(): Boolean = getRole() == "admin"

    fun isLoggedIn(): Boolean = getToken() != null

    fun logout() {
        prefs.edit().clear().apply()
    }
}
