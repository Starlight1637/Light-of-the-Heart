package com.mindful.companion.ui.screens.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindful.companion.data.api.DailyMoodData
import com.mindful.companion.data.api.MoodDistribution
import com.mindful.companion.data.api.WeeklyReportApiService
import com.mindful.companion.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeeklyReportViewModel @Inject constructor(
    private val weeklyReportApiService: WeeklyReportApiService,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeeklyReportUiState())
    val uiState: StateFlow<WeeklyReportUiState> = _uiState.asStateFlow()

    init {
        loadWeeklyReport()
    }

    fun loadWeeklyReport(week: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val token = authRepository.getToken()
                    ?: run {
                        _uiState.update { it.copy(isLoading = false, error = "未登录") }
                        return@launch
                    }
                val response = weeklyReportApiService.getWeeklyReport("Bearer $token", week)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            week = body.week,
                            dailyMoods = body.daily_moods,
                            dominantMood = body.dominant_mood,
                            moodDistribution = body.mood_distribution,
                            aiSummary = body.ai_summary,
                            totalPosts = body.total_posts
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "获取周报失败") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "网络错误: ${e.message}") }
            }
        }
    }
}

data class WeeklyReportUiState(
    val isLoading: Boolean = false,
    val week: String = "",
    val dailyMoods: List<DailyMoodData> = emptyList(),
    val dominantMood: String = "平静",
    val moodDistribution: MoodDistribution? = null,
    val aiSummary: String = "",
    val totalPosts: Int = 0,
    val error: String? = null
)
