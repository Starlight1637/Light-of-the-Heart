package com.mindful.companion.data.model

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
    val message: String,
    val role: String = "user"
)

data class ChangePasswordRequest(
    val old_password: String,
    val new_password: String
)

data class ChangePasswordResponse(
    val message: String
)
