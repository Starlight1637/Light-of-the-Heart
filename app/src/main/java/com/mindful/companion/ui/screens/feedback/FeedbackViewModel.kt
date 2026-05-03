package com.mindful.companion.ui.screens.feedback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindful.companion.data.model.FeedbackItem
import com.mindful.companion.data.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing feedback list state
 * Requirements: 5.4
 */
@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(FeedbackUiState())
    val uiState: StateFlow<FeedbackUiState> = _uiState.asStateFlow()
    
    init {
        loadFeedback()
    }
    
    /**
     * Load all user feedback from the server
     * Requirements: 5.4
     */
    fun loadFeedback() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            adminRepository.getFeedback().onSuccess { items ->
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        feedbackItems = items
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
}

/**
 * UI state for the feedback screen
 */
data class FeedbackUiState(
    val isLoading: Boolean = false,
    val feedbackItems: List<FeedbackItem> = emptyList(),
    val error: String? = null
)
