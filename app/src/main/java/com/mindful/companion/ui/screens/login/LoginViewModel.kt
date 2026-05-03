package com.mindful.companion.ui.screens.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindful.companion.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggedIn = authRepository.isLoggedIn()) }
        }
    }

    fun updateAccount(account: String) {
        _uiState.update { it.copy(account = account, errorMessage = null) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun selectSchool(school: String) {
        _uiState.update { it.copy(selectedSchool = school) }
    }

    fun toggleSchoolDropdown() {
        _uiState.update { it.copy(schoolDropdownExpanded = !it.schoolDropdownExpanded) }
    }

    fun login() {
        val state = _uiState.value

        if (state.account.isBlank()) {
            _uiState.update { it.copy(errorMessage = "请输入账号") }
            return
        }

        if (state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "请输入密码") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = authRepository.login(
                school = state.selectedSchool,
                account = state.account,
                password = state.password
            )

            result.onSuccess { loginResponse ->
                authRepository.saveLoginInfo(
                    token = loginResponse.token,
                    userId = loginResponse.user_id,
                    school = loginResponse.school,
                    account = loginResponse.account,
                    role = loginResponse.role
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        isAdmin = loginResponse.role == "admin",
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "登录失败，请检查账号和密码"
                    )
                }
            }
        }
    }
}

data class LoginUiState(
    val schools: List<String> = listOf("心光大学"),
    val selectedSchool: String = "心光大学",
    val schoolDropdownExpanded: Boolean = false,
    val account: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val isAdmin: Boolean = false,
    val errorMessage: String? = null
)
