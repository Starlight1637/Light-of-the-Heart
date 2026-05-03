package com.mindful.companion.ui.screens.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindful.companion.data.api.EmotionAnalysis
import com.mindful.companion.data.api.EmotionData
import com.mindful.companion.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CreatePostUiState())
    val uiState: StateFlow<CreatePostUiState> = _uiState.asStateFlow()
    
    fun updateContent(content: String) {
        _uiState.update { it.copy(content = content, error = null) }
    }
    
    fun analyzeEmotion() {
        val content = _uiState.value.content
        if (content.isBlank()) {
            _uiState.update { it.copy(error = "请先输入内容") }
            return
        }
        
        _uiState.update { it.copy(isAnalyzing = true, error = null) }
        
        viewModelScope.launch {
            try {
                val result = postRepository.analyzeEmotion(content)
                
                result.onSuccess { analysis ->
                    _uiState.update { 
                        it.copy(
                            isAnalyzing = false,
                            emotionAnalysis = analysis,
                            error = null
                        )
                    }
                }.onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            isAnalyzing = false,
                            error = error.message ?: "分析失败"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isAnalyzing = false,
                        error = "网络连接失败"
                    )
                }
            }
        }
    }
    
    fun publishPost() {
        val state = _uiState.value
        if (state.content.isBlank()) {
            _uiState.update { it.copy(error = "请先输入内容") }
            return
        }
        
        _uiState.update { it.copy(isPublishing = true, error = null) }
        
        viewModelScope.launch {
            try {
                // 转换情绪数据
                val emotionData = state.emotionAnalysis?.let {
                    EmotionData(
                        happiness = it.happiness,
                        sadness = it.sadness,
                        anger = it.anger,
                        fear = it.fear,
                        anxiety = it.anxiety,
                        overall = it.overall
                    )
                }
                
                // 判断风险等级
                val riskLevel = state.emotionAnalysis?.let { analysis ->
                    when {
                        analysis.riskKeywords.isNotEmpty() -> "CRITICAL"
                        analysis.overall < 0.3f -> "HIGH"
                        analysis.overall < 0.5f -> "MEDIUM"
                        else -> "LOW"
                    }
                } ?: "LOW"
                
                val result = postRepository.createPost(
                    content = state.content,
                    emotionData = emotionData,
                    riskLevel = riskLevel
                )
                
                result.onSuccess { postResponse ->
                    _uiState.update { 
                        it.copy(
                            isPublishing = false,
                            aiResponse = postResponse.ai_response,
                            publishSuccess = true,
                            error = null
                        )
                    }
                }.onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            isPublishing = false,
                            error = error.message ?: "发布失败"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isPublishing = false,
                        error = "网络连接失败"
                    )
                }
            }
        }
    }
}

data class CreatePostUiState(
    val content: String = "",
    val emotionAnalysis: EmotionAnalysis? = null,
    val aiResponse: String? = null,
    val isAnalyzing: Boolean = false,
    val isPublishing: Boolean = false,
    val publishSuccess: Boolean = false,
    val error: String? = null
)
