package com.mindful.companion.ui.screens.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindful.companion.data.model.WatchListItem
import com.mindful.companion.data.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing watch list state
 * Requirements: 2.2, 2.4
 */
@HiltViewModel
class WatchListViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(WatchListUiState())
    val uiState: StateFlow<WatchListUiState> = _uiState.asStateFlow()
    
    init {
        loadWatchList()
    }
    
    /**
     * Load the watch list from the server
     * Requirements: 2.2
     */
    fun loadWatchList() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            adminRepository.getWatchList().onSuccess { items ->
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        watchListItems = items
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
     * Mark a student as handled
     * Requirements: 2.4
     */
    fun markAsHandled(userId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            
            adminRepository.markAsHandled(userId).onSuccess { message ->
                // Reload the watch list to reflect the updated status
                loadWatchList()
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
 * UI state for the watch list screen
 */
data class WatchListUiState(
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val watchListItems: List<WatchListItem> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)
