package com.mindful.companion.ui.screens.batch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindful.companion.data.model.BatchAccountRequest
import com.mindful.companion.data.model.BatchAccountResponse
import com.mindful.companion.data.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing batch account creation
 * Requirements: 6.2, 6.4
 */
@HiltViewModel
class BatchAccountViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BatchAccountUiState())
    val uiState: StateFlow<BatchAccountUiState> = _uiState.asStateFlow()
    
    /**
     * Update the school selection
     */
    fun updateSchool(school: String) {
        _uiState.update { it.copy(school = school) }
    }
    
    /**
     * Update the account start value
     */
    fun updateAccountStart(accountStart: String) {
        _uiState.update { it.copy(accountStart = accountStart) }
    }
    
    /**
     * Update the account end value
     */
    fun updateAccountEnd(accountEnd: String) {
        _uiState.update { it.copy(accountEnd = accountEnd) }
    }
    
    /**
     * Update the role selection
     */
    fun updateRole(role: String) {
        _uiState.update { it.copy(role = role) }
    }
    
    /**
     * Validate the account range format
     * Requirements: 6.2
     */
    private fun validateAccountRange(): String? {
        val state = _uiState.value
        
        if (state.school.isBlank()) {
            return "请选择学校"
        }
        
        if (state.accountStart.isBlank() || state.accountEnd.isBlank()) {
            return "请输入账号范围"
        }
        
        val start = state.accountStart.toIntOrNull()
        val end = state.accountEnd.toIntOrNull()
        
        if (start == null || end == null) {
            return "账号必须是数字"
        }
        
        if (start > end) {
            return "起始账号不能大于结束账号"
        }
        
        if (end - start > 1000) {
            return "单次最多创建1000个账号"
        }
        
        return null
    }
    
    /**
     * Create batch accounts
     * Requirements: 6.4
     */
    fun createBatchAccounts() {
        val validationError = validateAccountRange()
        if (validationError != null) {
            _uiState.update { it.copy(error = validationError) }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, result = null) }
            
            val state = _uiState.value
            val request = BatchAccountRequest(
                school = state.school,
                accountStart = state.accountStart,
                accountEnd = state.accountEnd,
                role = state.role
            )
            
            adminRepository.createBatchAccounts(request).onSuccess { response ->
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        result = response
                    )
                }
            }.onFailure { error ->
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            }
        }
    }
    
    /**
     * Clear error message after it's been shown
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * Clear result to allow creating another batch
     */
    fun clearResult() {
        _uiState.update { it.copy(result = null) }
    }
    
    /**
     * Reset the form to initial state
     */
    fun resetForm() {
        _uiState.update { 
            BatchAccountUiState(
                school = it.school // Keep school selection
            )
        }
    }
}

/**
 * UI state for the batch account creation screen
 */
data class BatchAccountUiState(
    val isLoading: Boolean = false,
    val school: String = "",
    val accountStart: String = "",
    val accountEnd: String = "",
    val role: String = "user", // Default to "user" role
    val result: BatchAccountResponse? = null,
    val error: String? = null
)
