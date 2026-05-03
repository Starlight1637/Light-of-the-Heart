package com.mindful.companion.ui.screens.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindful.companion.data.model.AdminReport
import com.mindful.companion.data.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing admin reports state
 * Requirements: 4.2, 4.4
 */
@HiltViewModel
class AdminReportsViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AdminReportsUiState())
    val uiState: StateFlow<AdminReportsUiState> = _uiState.asStateFlow()
    
    init {
        loadReports()
    }
    
    /**
     * Load all reports sent to admin by students
     * Requirements: 4.2
     */
    fun loadReports() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            adminRepository.getAdminReports().onSuccess { reports ->
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        reports = reports
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
     * Mark a report as reviewed
     * Requirements: 4.4
     */
    fun markReportReviewed(reportId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            
            adminRepository.markReportReviewed(reportId).onSuccess { message ->
                // Reload the reports to reflect the updated status
                loadReports()
                _uiState.update { 
                    it.copy(
                        isProcessing = false,
                        successMessage = message
                    )
                }
            }.onFailure { error ->
                _uiState.update { 
                    it.copy(
                        isProcessing = false,
                        error = error.message
                    )
                }
            }
        }
    }
    
    /**
     * Clear success message after it's been shown
     */
    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
    
    /**
     * Clear error message after it's been shown
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * UI state for the admin reports screen
 */
data class AdminReportsUiState(
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val reports: List<AdminReport> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)
