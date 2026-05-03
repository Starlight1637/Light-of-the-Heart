package com.mindful.companion.ui.screens.watchlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindful.companion.data.model.FlaggedEntry
import com.mindful.companion.data.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for watch list detail screen
 * Requirements: 2.3, 2.4
 */
@HiltViewModel
class WatchListDetailViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val userId: Int = savedStateHandle.get<String>("userId")?.toIntOrNull() ?: 0
    
    private val _uiState = MutableStateFlow(WatchListDetailUiState())
    val uiState: StateFlow<WatchListDetailUiState> = _uiState.asStateFlow()
    
    init {
        loadFlaggedEntries()
    }
    
    /**
     * Load flagged diary entries for the user
     * Requirements: 2.3
     */
    fun loadFlaggedEntries() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            adminRepository.getFlaggedEntries(userId).onSuccess { entries ->
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        flaggedEntries = entries,
                        userId = userId
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
     * Mark the student as handled
     * Requirements: 2.4
     */
    fun markAsHandled() {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            
            adminRepository.markAsHandled(userId).onSuccess { message ->
                _uiState.update { 
                    it.copy(
                        isProcessing = false,
                        successMessage = message,
                        isHandled = true
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
 * UI state for the watch list detail screen
 */
data class WatchListDetailUiState(
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val userId: Int = 0,
    val flaggedEntries: List<FlaggedEntry> = emptyList(),
    val isHandled: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)
