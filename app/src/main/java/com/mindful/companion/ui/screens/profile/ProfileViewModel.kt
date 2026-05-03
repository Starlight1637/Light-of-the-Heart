package com.mindful.companion.ui.screens.profile

import androidx.lifecycle.ViewModel
import com.mindful.companion.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    fun isLoggedIn(): Boolean = authRepository.isLoggedIn()
    
    fun getUserInfo(): UserInfo {
        return if (isLoggedIn()) {
            UserInfo(
                school = authRepository.getSchool() ?: "",
                account = authRepository.getAccount() ?: ""
            )
        } else {
            UserInfo("", "")
        }
    }
    
    fun logout() {
        authRepository.logout()
    }
}

data class UserInfo(
    val school: String,
    val account: String
)
