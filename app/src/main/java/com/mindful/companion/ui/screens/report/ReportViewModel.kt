package com.mindful.companion.ui.screens.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindful.companion.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()
    
    fun selectReportType(type: ReportType) {
        _uiState.update { it.copy(reportType = type) }
        loadReports()
    }
    
    fun loadReports() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // 暂时使用空列表，实际应该从服务器获取
                val postList = emptyList<com.mindful.companion.data.model.Post>()
                
                val report = when (_uiState.value.reportType) {
                    ReportType.WEEK -> generateWeekReport(postList)
                    ReportType.MONTH -> generateMonthReport(postList)
                }
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        report = report,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "加载失败: ${e.message}"
                    )
                }
            }
        }
    }
    
    private fun generateWeekReport(posts: List<com.mindful.companion.data.model.Post>): PsychologicalReport? {
        val calendar = Calendar.getInstance()
        val endDate = calendar.time
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val startDate = calendar.time
        
        val weekPosts = posts.filter { post ->
            val postDate = parseDate(post.timestamp.toString())
            postDate != null && postDate.after(startDate) && postDate.before(endDate)
        }
        
        if (weekPosts.isEmpty()) return null
        
        return generateReport(
            posts = weekPosts,
            title = "本周心理报告",
            period = "${formatDate(startDate)} - ${formatDate(endDate)}"
        )
    }
    
    private fun generateMonthReport(posts: List<com.mindful.companion.data.model.Post>): PsychologicalReport? {
        val calendar = Calendar.getInstance()
        val endDate = calendar.time
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        val startDate = calendar.time
        
        val monthPosts = posts.filter { post ->
            val postDate = parseDate(post.timestamp.toString())
            postDate != null && postDate.after(startDate) && postDate.before(endDate)
        }
        
        if (monthPosts.isEmpty()) return null
        
        return generateReport(
            posts = monthPosts,
            title = "本月心理报告",
            period = "${formatDate(startDate)} - ${formatDate(endDate)}"
        )
    }
    
    private fun generateReport(
        posts: List<com.mindful.companion.data.model.Post>,
        title: String,
        period: String
    ): PsychologicalReport {
        // 计算情绪趋势
        val emotionTrend = posts.mapNotNull { post ->
            val date = parseDate(post.timestamp.toString())
            val emotion = 0.5 // 默认值，实际应该从 post 中获取
            if (date != null) {
                EmotionDataPoint(
                    date = formatShortDate(date),
                    value = emotion.toFloat()
                )
            } else null
        }.sortedBy { it.date }
        
        // 计算平均情绪
        val averageEmotion = emotionTrend.map { it.value }.average().toFloat()
        
        // 计算情绪分布
        val emotionDistribution = calculateEmotionDistribution(posts)
        
        // 生成 AI 总结
        val aiSummary = generateAISummary(posts, averageEmotion, emotionDistribution)
        
        // 生成建议
        val suggestions = generateSuggestions(averageEmotion, emotionDistribution)
        
        return PsychologicalReport(
            title = title,
            period = period,
            recordDays = posts.size,
            averageEmotion = averageEmotion,
            emotionTrend = emotionTrend,
            emotionDistribution = emotionDistribution,
            aiSummary = aiSummary,
            suggestions = suggestions
        )
    }
    
    private fun calculateEmotionDistribution(posts: List<com.mindful.companion.data.model.Post>): Map<String, Float> {
        if (posts.isEmpty()) {
            return mapOf("平静" to 1.0f)
        }
        
        val distribution = mutableMapOf<String, Int>()
        
        posts.forEach {
            // 默认为平静，实际应该从 post 中获取情绪数据
            val dominant = "平静"
            distribution[dominant] = (distribution[dominant] ?: 0) + 1
        }
        
        val total = distribution.values.sum().toFloat()
        return distribution.mapValues { it.value / total }
    }
    
    private fun generateAISummary(
        posts: List<com.mindful.companion.data.model.Post>,
        averageEmotion: Float,
        distribution: Map<String, Float>
    ): String {
        val dominantEmotion = distribution.maxByOrNull { it.value }?.key ?: "平静"
        val emotionLevel = when {
            averageEmotion >= 0.7 -> "良好"
            averageEmotion >= 0.5 -> "一般"
            else -> "需要关注"
        }
        
        return buildString {
            append("在这段时间里，你记录了 ${posts.size} 次心情。")
            append("整体情绪状态${emotionLevel}，平均情绪指数为 ${String.format("%.1f", averageEmotion * 100)} 分。")
            append("\n\n")
            append("你最常出现的情绪是「${dominantEmotion}」，")
            append("占比 ${String.format("%.0f", (distribution[dominantEmotion] ?: 0f) * 100)}%。")
            append("\n\n")
            
            when {
                averageEmotion >= 0.7 -> {
                    append("你的情绪状态很不错！继续保持积极乐观的心态，")
                    append("记得在忙碌之余给自己一些放松的时间。")
                }
                averageEmotion >= 0.5 -> {
                    append("你的情绪有起伏是很正常的。")
                    append("试着找到让自己开心的事情，多和朋友家人交流，")
                    append("适当运动也能帮助改善心情。")
                }
                else -> {
                    append("最近你可能经历了一些困难。")
                    append("请记住，寻求帮助是勇敢的表现。")
                    append("如果情绪持续低落，建议咨询专业的心理咨询师。")
                }
            }
        }
    }
    
    private fun generateSuggestions(
        averageEmotion: Float,
        distribution: Map<String, Float>
    ): List<String> {
        val suggestions = mutableListOf<String>()
        
        if (averageEmotion < 0.5) {
            suggestions.add("每天花10分钟做呼吸练习，帮助放松身心")
            suggestions.add("尝试写下让你感恩的三件事")
            suggestions.add("保持规律的作息，充足的睡眠很重要")
        }
        
        val sadnessRatio = distribution["悲伤"] ?: 0f
        if (sadnessRatio > 0.3) {
            suggestions.add("多参加户外活动，阳光和运动能改善心情")
            suggestions.add("和信任的朋友倾诉，不要独自承受")
        }
        
        val anxietyRatio = distribution["焦虑"] ?: 0f
        if (anxietyRatio > 0.3) {
            suggestions.add("尝试冥想或正念练习，专注当下")
            suggestions.add("列出让你焦虑的事情，逐一制定应对计划")
        }
        
        if (suggestions.isEmpty()) {
            suggestions.add("继续保持记录心情的习惯")
            suggestions.add("尝试新的放松方式，如听音乐、画画等")
            suggestions.add("定期回顾自己的成长和进步")
        }
        
        return suggestions
    }
    
    private fun parseDate(dateString: String): Date? {
        return try {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(dateString)
        } catch (e: Exception) {
            null
        }
    }
    
    private fun formatDate(date: Date): String {
        return SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault()).format(date)
    }
    
    private fun formatShortDate(date: Date): String {
        return SimpleDateFormat("MM/dd", Locale.getDefault()).format(date)
    }
}

data class ReportUiState(
    val reportType: ReportType = ReportType.WEEK,
    val isLoading: Boolean = false,
    val report: PsychologicalReport? = null,
    val error: String? = null
)

enum class ReportType {
    WEEK, MONTH
}

data class PsychologicalReport(
    val title: String,
    val period: String,
    val recordDays: Int,
    val averageEmotion: Float,
    val emotionTrend: List<EmotionDataPoint>,
    val emotionDistribution: Map<String, Float>,
    val aiSummary: String,
    val suggestions: List<String>
)

data class EmotionDataPoint(
    val date: String,
    val value: Float
)
