package com.mindful.companion.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindful.companion.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPostsViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val chatRepository: com.mindful.companion.data.repository.ChatRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MyPostsUiState())
    val uiState: StateFlow<MyPostsUiState> = _uiState.asStateFlow()
    
    fun loadMyPosts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // 加载聊天会话（心情报告）
                val sessions = chatRepository.getAllSessions()
                val sessionItems = sessions
                    .filter { session -> session.moodReport != null } // 只显示有心情报告的会话
                    .map { session ->
                        PostHistoryItem(
                            id = session.sessionId.hashCode(),
                            content = session.moodReport ?: "",
                            aiResponse = null,
                            createdAt = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                                .format(session.endTime ?: session.startTime),
                            isSession = true
                        )
                    }
                
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        posts = sessionItems,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        error = e.message ?: "加载失败"
                    )
                }
            }
        }
    }
}

data class MyPostsUiState(
    val isLoading: Boolean = false,
    val posts: List<PostHistoryItem> = emptyList(),
    val error: String? = null
)

data class PostHistoryItem(
    val id: Int,
    val content: String,
    val aiResponse: String?,
    val createdAt: String,
    val isSession: Boolean = false // 标识是否是聊天会话报告
)
