package com.mindful.companion.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindful.companion.data.repository.AIRepository
import com.mindful.companion.data.api.ZhipuChatRequest
import com.mindful.companion.data.api.ZhipuMessage
import com.mindful.companion.data.model.SimpleMessage
import com.mindful.companion.data.repository.ChatRepository
import com.mindful.companion.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val aiRepository: AIRepository,
    private val zhipuAIService: com.mindful.companion.data.api.ZhipuAIService,
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    private var currentSessionId: String = ""
    
    init {
        // 监听消息流
        viewModelScope.launch {
            chatRepository.messagesFlow.collect { messages ->
                val uiMessages = messages.map { msg ->
                    ChatMessage(
                        id = msg.id,
                        content = msg.content,
                        isFromUser = msg.isFromUser,
                        timestamp = msg.timestamp
                    )
                }
                _uiState.update { it.copy(messages = uiMessages) }
            }
        }
        
        loadOrCreateSession()
    }
    
    private fun loadOrCreateSession() {
        viewModelScope.launch {
            try {
                val session = chatRepository.getOrCreateActiveSession()
                currentSessionId = session.sessionId
                _uiState.update { it.copy(currentSessionId = currentSessionId) }
                
                // 加载历史消息
                val messages = chatRepository.loadMessagesForSession(currentSessionId)
                
                if (messages.isEmpty()) {
                    // 如果没有消息，添加欢迎消息
                    addWelcomeMessage()
                }
            } catch (e: Exception) {
                // 如果加载失败，尝试添加欢迎消息
                if (currentSessionId.isNotBlank()) {
                    addWelcomeMessage()
                }
            }
        }
    }
    
    private fun addWelcomeMessage() {
        if (currentSessionId.isBlank()) return
        
        viewModelScope.launch {
            try {
                val greeting = getTimeBasedGreeting()
                val welcomeMessage = com.mindful.companion.data.model.ChatMessage(
                    id = UUID.randomUUID().toString(),
                    userId = authRepository.getUserId(),
                    content = "$greeting 有什么想聊的吗？开心的、烦恼的都可以，我会认真听的",
                    isFromUser = false,
                    timestamp = Date(),
                    sessionId = currentSessionId
                )
                
                chatRepository.saveMessage(welcomeMessage)
            } catch (e: Exception) {
                // 忽略保存失败
            }
        }
    }
    
    private fun getTimeBasedGreeting(): String {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        
        return when (hour) {
            in 5..11 -> "早上好！"
            in 12..13 -> "中午好！"
            in 14..17 -> "下午好！"
            in 18..23 -> "晚上好！"
            else -> "夜深了，"
        }
    }
    
    fun updateInputText(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }
    
    fun sendMessage() {
        val userMessage = _uiState.value.inputText.trim()
        if (userMessage.isBlank() || currentSessionId.isBlank()) return
        val sessionHistory = _uiState.value.messages
            .takeLast(8)
            .map {
                SimpleMessage(
                    role = if (it.isFromUser) "user" else "assistant",
                    content = it.content
                )
            }
        
        viewModelScope.launch {
            // 保存用户消息
            val message = com.mindful.companion.data.model.ChatMessage(
                id = UUID.randomUUID().toString(),
                userId = authRepository.getUserId(),
                content = userMessage,
                isFromUser = true,
                timestamp = Date(),
                sessionId = currentSessionId
            )
            chatRepository.saveMessage(message)
            
            _uiState.update { 
                it.copy(
                    inputText = "",
                    isAiTyping = true,
                    showEndButton = false
                )
            }
            
            // 获取AI回复
            try {
                val aiResponse = aiRepository.generateChatResponse(
                    userMessage = userMessage,
                    sessionHistory = sessionHistory
                ).getOrThrow().message
                
                val aiMessage = com.mindful.companion.data.model.ChatMessage(
                    id = UUID.randomUUID().toString(),
                    userId = authRepository.getUserId(),
                    content = aiResponse,
                    isFromUser = false,
                    timestamp = Date(),
                    sessionId = currentSessionId
                )
                chatRepository.saveMessage(aiMessage)
                
                _uiState.update { 
                    it.copy(
                        isAiTyping = false,
                        showEndButton = true
                    )
                }
            } catch (e: Exception) {
                // 如果API调用失败，使用本地回复
                val aiResponse = generateAIResponse(userMessage)
                
                val aiMessage = com.mindful.companion.data.model.ChatMessage(
                    id = UUID.randomUUID().toString(),
                    userId = authRepository.getUserId(),
                    content = aiResponse,
                    isFromUser = false,
                    timestamp = Date(),
                    sessionId = currentSessionId
                )
                chatRepository.saveMessage(aiMessage)
                
                _uiState.update { 
                    it.copy(
                        isAiTyping = false,
                        showEndButton = true
                    )
                }
            }
        }
    }
    
    fun endChatSession() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingReport = true) }
            
            try {
                val report = generateMoodReport()
                chatRepository.endSession(currentSessionId, report)
                
                _uiState.update { 
                    it.copy(
                        isGeneratingReport = false,
                        moodReport = report,
                        showEndButton = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isGeneratingReport = false,
                        showEndButton = true
                    )
                }
            }
        }
    }
    
    fun startNewChat() {
        viewModelScope.launch {
            try {
                val newSession = chatRepository.startNewSession()
                currentSessionId = newSession.sessionId
                
                _uiState.update { 
                    ChatUiState(currentSessionId = currentSessionId)
                }
                
                // 添加欢迎消息
                addWelcomeMessage()
            } catch (e: Exception) {
                // 创建失败
            }
        }
    }
    
    fun dismissMoodReport() {
        _uiState.update { it.copy(moodReport = null) }
        startNewChat()
    }
    
    private suspend fun generateMoodReport(): String {
        val messages = _uiState.value.messages
        val userMessages = messages.filter { it.isFromUser }.map { it.content }
        
        if (userMessages.isEmpty()) {
            return "今天的聊天时间有点短，下次多聊聊吧"
        }
        
        val messageCount = messages.size
        val duration = if (messages.isNotEmpty()) {
            val start = messages.first().timestamp
            val end = messages.last().timestamp
            ((end.time - start.time) / 60000).toInt()
        } else 0
        
        return try {
            val conversationSummary = buildString {
                appendLine("请分析以下对话内容，生成一份简洁的心情报告：")
                appendLine()
                messages.forEach { msg ->
                    if (msg.isFromUser) {
                        appendLine("用户：${msg.content}")
                    }
                }
                appendLine()
                appendLine("请按以下格式生成报告：")
                appendLine("1. 情绪状态（积极/平和/需要关注）")
                appendLine("2. 简短的情绪分析（2-3句话）")
                appendLine("3. 温暖的建议或鼓励（1-2句话）")
            }
            
            val aiMessages = listOf(
                ZhipuMessage(
                    role = "system",
                    content = "你是一个专业的心理健康分析师。请根据用户的对话内容，分析其情绪状态并生成简洁、温暖的心情报告。"
                ),
                ZhipuMessage(
                    role = "user",
                    content = conversationSummary
                )
            )
            
            val request = ZhipuChatRequest(
                model = "deepseek-chat",
                messages = aiMessages,
                temperature = 0.7f,
                top_p = 0.9f,
                max_tokens = 500
            )
            
            val token = authRepository.getToken() ?: throw Exception("未登录")
            val response = zhipuAIService.chatCompletions("Bearer $token", request)
            
            val aiReport = if (response.isSuccessful && response.body() != null) {
                response.body()!!.choices.firstOrNull()?.message?.content 
                    ?: generateFallbackReport(userMessages, messageCount, duration)
            } else {
                generateFallbackReport(userMessages, messageCount, duration)
            }
            
            buildString {
                appendLine("本次聊天心情报告")
                appendLine()
                appendLine("聊天时长：${if (duration > 0) "${duration}分钟" else "不到1分钟"}")
                appendLine("消息数量：${messageCount}条")
                appendLine()
                append(aiReport)
            }
        } catch (e: Exception) {
            generateFallbackReport(userMessages, messageCount, duration)
        }
    }
    
    private fun generateFallbackReport(userMessages: List<String>, messageCount: Int, duration: Int): String {
        val allText = userMessages.joinToString(" ")
        
        val positiveKeywords = listOf("开心", "高兴", "快乐", "幸福", "满意", "好", "棒", "喜欢", "不错", "舒服")
        val negativeKeywords = listOf("难过", "伤心", "痛苦", "焦虑", "担心", "压力", "累", "疲惫", "孤独", "难受")
        
        val positiveCount = positiveKeywords.count { allText.contains(it) }
        val negativeCount = negativeKeywords.count { allText.contains(it) }
        
        val mood = when {
            negativeCount > positiveCount -> "需要关注"
            positiveCount > negativeCount -> "积极"
            else -> "平和"
        }
        
        return buildString {
            appendLine("本次聊天心情报告")
            appendLine()
            appendLine("聊天时长：${if (duration > 0) "${duration}分钟" else "不到1分钟"}")
            appendLine("消息数量：${messageCount}条")
            appendLine("情绪状态：$mood")
            appendLine()
            when (mood) {
                "积极" -> appendLine("很高兴看到你心情不错！保持这份好心情，继续加油！")
                "需要关注" -> {
                    appendLine("我注意到你可能有些烦恼。记得照顾好自己，必要时可以寻求专业帮助。")
                    appendLine()
                    appendLine("心理援助热线：400-161-9995")
                }
                else -> appendLine("今天的你很平静。记得多关注自己的感受，有需要随时来找我聊天。")
            }
        }
    }
    
    private fun generateAIResponse(userMessage: String): String {
        val lowerMessage = userMessage.lowercase()
        
        return when {
            lowerMessage.contains("你好") || lowerMessage.contains("hi") -> {
                "嘿！今天过得怎么样？有什么想聊的吗？"
            }
            lowerMessage.contains("难过") || lowerMessage.contains("伤心") -> {
                "听起来你现在心情不太好。能跟我说说发生什么了吗？我会好好听的"
            }
            lowerMessage.contains("焦虑") || lowerMessage.contains("紧张") -> {
                "焦虑的感觉真的很难受。深呼吸，慢慢来。你最担心的是什么呀？"
            }
            lowerMessage.contains("压力") || lowerMessage.contains("累") -> {
                "听起来你最近真的挺累的。要不要休息一下？跟我说说是什么让你这么累吧。"
            }
            lowerMessage.contains("谢谢") -> {
                "别客气呀！能陪你聊天我也很开心。记得照顾好自己哦"
            }
            else -> {
                listOf(
                    "我在听呢，继续说吧",
                    "听起来这对你挺重要的。你现在什么感觉？",
                    "嗯，我懂你的感受。这确实不容易",
                    "你能说出来就已经很勇敢了。我会一直陪着你的"
                ).random()
            }
        }
    }

    fun loadChatSessions() {
        viewModelScope.launch {
            try {
                val sessions = chatRepository.getAllSessions()
                val sessionItems = sessions.map { session ->
                    val messageCount = chatRepository.getMessageCountForSession(session.sessionId)
                    val firstMessage = chatRepository.getFirstUserMessageForSession(session.sessionId)
                    
                    val calendar = Calendar.getInstance().apply {
                        time = session.createdAt
                    }
                    val year = calendar.get(Calendar.YEAR)
                    val month = calendar.get(Calendar.MONTH) + 1
                    val day = calendar.get(Calendar.DAY_OF_MONTH)
                    
                    val sessionsOnSameDay = sessions.filter { s ->
                        val c = Calendar.getInstance().apply { time = s.createdAt }
                        c.get(Calendar.YEAR) == year &&
                        c.get(Calendar.MONTH) + 1 == month &&
                        c.get(Calendar.DAY_OF_MONTH) == day
                    }
                    val index = sessionsOnSameDay.indexOf(session) + 1
                    
                    ChatSessionItem(
                        id = session.sessionId,
                        title = "${year}年${month}月${day}日 心事${index}",
                        preview = firstMessage ?: "暂无内容",
                        timestamp = session.createdAt,
                        messageCount = messageCount
                    )
                }
                
                _uiState.update { it.copy(chatSessions = sessionItems) }
            } catch (e: Exception) {
                // 加载失败
            }
        }
    }
    
    fun loadSession(sessionId: String) {
        viewModelScope.launch {
            try {
                currentSessionId = sessionId
                _uiState.update { it.copy(currentSessionId = sessionId) }
                chatRepository.loadMessagesForSession(sessionId)
            } catch (e: Exception) {
                // 加载失败
            }
        }
    }
    
    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            try {
                chatRepository.deleteSession(sessionId)
                
                if (sessionId == currentSessionId) {
                    startNewChat()
                }
                
                // 重新加载会话列表
                loadChatSessions()
            } catch (e: Exception) {
                // 删除失败
            }
        }
    }
    
    fun showSendToAdminDialog() {
        _uiState.update { it.copy(showSendToAdminDialog = true) }
    }
    
    fun dismissSendToAdminDialog() {
        _uiState.update { 
            it.copy(
                showSendToAdminDialog = false,
                sendToAdminSuccess = null
            )
        }
    }
    
    fun sendReportToAdmin() {
        if (currentSessionId.isBlank()) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSendingToAdmin = true) }
            
            try {
                val result = chatRepository.sendSessionToAdmin(currentSessionId)
                
                if (result.isSuccess) {
                    _uiState.update { 
                        it.copy(
                            isSendingToAdmin = false,
                            sendToAdminSuccess = true
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isSendingToAdmin = false,
                            sendToAdminSuccess = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isSendingToAdmin = false,
                        sendToAdminSuccess = false
                    )
                }
            }
        }
    }
}

data class ChatSessionItem(
    val id: String,
    val title: String,
    val preview: String,
    val timestamp: Date,
    val messageCount: Int
)

data class ChatMessage(
    val id: String,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Date
)

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isAiTyping: Boolean = false,
    val showEndButton: Boolean = false,
    val isGeneratingReport: Boolean = false,
    val moodReport: String? = null,
    val chatSessions: List<ChatSessionItem> = emptyList(),
    val currentSessionId: String? = null,
    val showSendToAdminDialog: Boolean = false,
    val isSendingToAdmin: Boolean = false,
    val sendToAdminSuccess: Boolean? = null
)
