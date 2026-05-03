package com.mindful.companion.ui.screens.feedback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindful.companion.data.repository.FeedbackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for managing feedback submission state
 * Requirements: 5.4
 */
@HiltViewModel
class FeedbackSubmitViewModel @Inject constructor(
    private val feedbackRepository: FeedbackRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(FeedbackSubmitUiState())
    val uiState: StateFlow<FeedbackSubmitUiState> = _uiState.asStateFlow()
    
    /**
     * Update feedback content
     */
    fun updateContent(content: String) {
        _uiState.update { it.copy(content = content) }
    }
    
    /**
     * Submit feedback to admin
     * Requirements: 5.4
     */
    fun submitFeedback() {
        val content = _uiState.value.content.trim()
        
        if (content.isEmpty()) {
            _uiState.update { it.copy(error = "反馈内容不能为空") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            
            val feedbackId = UUID.randomUUID().toString()
            
            feedbackRepository.submitFeedback(feedbackId, content).onSuccess {
                _uiState.update { 
                    it.copy(
                        isSubmitting = false,
                        submitSuccess = true,
                        successMessage = "反馈提交成功，感谢您的建议！"
                    )
                }
            }.onFailure { error ->
                _uiState.update { 
                    it.copy(
                        isSubmitting = false,
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
     * Reset the form after successful submission
     */
    fun resetForm() {
        _uiState.update { FeedbackSubmitUiState() }
    }
}

/**
 * UI state for the feedback submission screen
 */
data class FeedbackSubmitUiState(
    val content: String = "",
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val successMessage: String? = null,
    val error: String? = null
)
