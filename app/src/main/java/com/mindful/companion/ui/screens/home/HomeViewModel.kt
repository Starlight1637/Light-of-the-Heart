package com.mindful.companion.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindful.companion.data.model.Post
import com.mindful.companion.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val postDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadPosts()
    }

    private fun loadPosts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = postRepository.getPosts()
            result.onSuccess { postResponses ->
                val posts = postResponses.map { response ->
                    Post(
                        id = response.id.toString(),
                        content = response.content,
                        timestamp = parseCreatedAt(response.created_at),
                        emotionScore = response.emotion_data?.overall
                    )
                }
                val weeklyBars = buildWeeklyBarsFromPosts(posts)
                val dominant = computeDominantMood(posts)
                val todayCount = countTodayPosts(posts)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        posts = posts,
                        error = null,
                        greetingText = getGreetingText(),
                        weeklyMoodBars = weeklyBars,
                        dominantMood = dominant,
                        todayPostCount = todayCount
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "加载失败",
                        greetingText = getGreetingText()
                    )
                }
            }
        }
    }

    fun refreshPosts() {
        loadPosts()
    }

    private fun getGreetingText(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "早上好"
            hour < 18 -> "下午好"
            else -> "晚上好"
        }
    }

    private fun parseCreatedAt(createdAt: String): Date {
        val normalized = createdAt.substringBefore(".")
        return try {
            postDateFormat.parse(normalized) ?: Date()
        } catch (e: Exception) {
            Date()
        }
    }

    private fun buildWeeklyBarsFromPosts(posts: List<Post>): List<Float> {
        val bars = mutableListOf<Float>()
        for (dayOffset in 6 downTo 0) {
            val target = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -dayOffset) }
            val dayPosts = posts.filter { post ->
                val postCal = Calendar.getInstance().apply { time = post.timestamp }
                postCal.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR) &&
                    postCal.get(Calendar.YEAR) == target.get(Calendar.YEAR)
            }
            val avg = if (dayPosts.isEmpty()) 0.5f
            else {
                val scores = dayPosts.map { it.emotionScore ?: 0.5f }
                scores.average().toFloat().let { if (it.isNaN()) 0.5f else it }
            }
            bars.add(avg)
        }
        return bars
    }

    private fun computeDominantMood(posts: List<Post>): String {
        if (posts.isEmpty()) return "平静"
        val avg = posts.mapNotNull { it.emotionScore }.average()
        return when {
            avg.isNaN() -> "平静"
            avg >= 0.7 -> "愉悦"
            avg >= 0.5 -> "平静"
            avg >= 0.3 -> "低落"
            else -> "困扰"
        }
    }

    private fun countTodayPosts(posts: List<Post>): Int {
        val today = Calendar.getInstance()
        return posts.count { post ->
            val postCal = Calendar.getInstance().apply { time = post.timestamp }
            postCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) &&
                postCal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
        }
    }
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val posts: List<Post> = emptyList(),
    val error: String? = null,
    val greetingText: String = "你好",
    val weeklyMoodBars: List<Float> = List(7) { 0.5f },
    val dominantMood: String = "平静",
    val todayPostCount: Int = 0
)
