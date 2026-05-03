package com.mindful.companion.data.repository

import com.mindful.companion.data.api.ChatApiService
import com.mindful.companion.data.api.ChatMessageCreateRequest
import com.mindful.companion.data.api.ChatSessionCreateRequest
import com.mindful.companion.data.api.ChatSessionUpdateRequest
import com.mindful.companion.data.model.ChatMessage
import com.mindful.companion.data.model.ChatSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val chatApiService: ChatApiService,
    private val authRepository: AuthRepository
) {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    
    // 内存缓存
    private var cachedSessions: MutableList<ChatSession> = mutableListOf()
    private var cachedMessages: MutableMap<String, MutableList<ChatMessage>> = mutableMapOf()
    private var currentActiveSessionId: String? = null
    
    // 消息状态流 - 用于实时更新UI
    private val _messagesFlow = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messagesFlow: StateFlow<List<ChatMessage>> = _messagesFlow.asStateFlow()
    
    private fun requireAuthToken(): String {
        val token = authRepository.getToken()
            ?: throw IllegalStateException("not_logged_in")
        return "Bearer $token"
    }
    
    suspend fun saveMessage(message: ChatMessage) {
        val token = requireAuthToken()
        // 先添加到缓存
        val sessionMessages = cachedMessages.getOrPut(message.sessionId) { mutableListOf() }
        sessionMessages.add(message)
        
        // 更新 Flow
        if (message.sessionId == currentActiveSessionId) {
            _messagesFlow.value = sessionMessages.toList()
        }
        
        // 保存到服务器
        try {
            val request = ChatMessageCreateRequest(
                id = message.id,
                session_id = message.sessionId,
                content = message.content,
                is_from_user = message.isFromUser,
                timestamp = dateFormat.format(message.timestamp)
            )
            val response = chatApiService.createMessage(token, request)
            if (!response.isSuccessful) {
                throw IllegalStateException("create_message_failed_${response.code()}")
            }
        } catch (e: Exception) {
            sessionMessages.removeAll { it.id == message.id }
            if (message.sessionId == currentActiveSessionId) {
                _messagesFlow.value = sessionMessages.toList()
            }
            throw e
            // 服务器保存失败，消息已在缓存中
        }
    }
    
    suspend fun loadMessagesForSession(sessionId: String): List<ChatMessage> {
        val token = requireAuthToken()
        currentActiveSessionId = sessionId
        
        // 先返回缓存
        val cached = cachedMessages[sessionId]
        if (cached != null && cached.isNotEmpty()) {
            _messagesFlow.value = cached.toList()
            return cached.toList()
        }
        
        // 从服务器获取
        try {
            val response = chatApiService.getSessionMessages(token, sessionId)
            if (response.isSuccessful && response.body() != null) {
                val messages = response.body()!!.map { msg ->
                    ChatMessage(
                        id = msg.id,
                        userId = msg.user_id,
                        content = msg.content,
                        isFromUser = msg.is_from_user,
                        timestamp = parseDate(msg.timestamp),
                        sessionId = msg.session_id
                    )
                }.toMutableList()
                
                cachedMessages[sessionId] = messages
                _messagesFlow.value = messages.toList()
                return messages.toList()
            }
        } catch (e: Exception) {
            // 加载失败
        }
        
        _messagesFlow.value = emptyList()
        return emptyList()
    }
    
    suspend fun getOrCreateActiveSession(): ChatSession {
        val token = requireAuthToken()
        val userId = authRepository.getUserId()
        
        // 检查缓存中的活跃会话
        if (currentActiveSessionId != null) {
            val cached = cachedSessions.find { it.sessionId == currentActiveSessionId && it.isActive }
            if (cached != null) return cached
        }
        
        // 从服务器获取活跃会话
        try {
            val response = chatApiService.getActiveSession(token)
            if (response.isSuccessful && response.body() != null) {
                val serverSession = response.body()!!
                val session = ChatSession(
                    sessionId = serverSession.id,
                    userId = userId,
                    startTime = parseDate(serverSession.start_time),
                    endTime = serverSession.end_time?.let { parseDate(it) },
                    moodReport = serverSession.mood_report,
                    isActive = serverSession.is_active
                )
                
                // 更新缓存
                cachedSessions.removeAll { it.sessionId == session.sessionId }
                cachedSessions.add(0, session)
                currentActiveSessionId = session.sessionId
                
                return session
            }
        } catch (e: Exception) {
            // 服务器获取失败
        }
        
        // 创建新会话
        return createNewSession(userId)
    }
    
    private suspend fun createNewSession(userId: Int): ChatSession {
        val token = requireAuthToken()
        val newSessionId = UUID.randomUUID().toString()
        val newSession = ChatSession(
            sessionId = newSessionId,
            userId = userId,
            startTime = Date(),
            isActive = true
        )
        
        // 保存到服务器
        try {
            val request = ChatSessionCreateRequest(session_id = newSessionId)
            val response = chatApiService.createSession(token, request)
            if (!response.isSuccessful) {
                throw IllegalStateException("create_session_failed_${response.code()}")
            }
        } catch (e: Exception) {
            throw e
            // 服务器保存失败
        }
        
        // 更新缓存
        cachedSessions.add(0, newSession)
        currentActiveSessionId = newSessionId
        cachedMessages[newSessionId] = mutableListOf()
        _messagesFlow.value = emptyList()
        
        return newSession
    }
    
    private fun parseDate(dateStr: String): Date {
        return try {
            dateFormat.parse(dateStr) ?: Date()
        } catch (e: Exception) {
            Date()
        }
    }
    
    suspend fun endSession(sessionId: String, moodReport: String) {
        val token = requireAuthToken()
        // 更新缓存
        val sessionIndex = cachedSessions.indexOfFirst { it.sessionId == sessionId }
        if (sessionIndex >= 0) {
            cachedSessions[sessionIndex] = cachedSessions[sessionIndex].copy(
                endTime = Date(),
                moodReport = moodReport,
                isActive = false
            )
        }
        
        if (currentActiveSessionId == sessionId) {
            currentActiveSessionId = null
        }
        
        // 同步到服务器
        try {
            val request = ChatSessionUpdateRequest(
                end_time = dateFormat.format(Date()),
                mood_report = moodReport,
                is_active = false
            )
            val response = chatApiService.updateSession(token, sessionId, request)
            if (!response.isSuccessful) {
                throw IllegalStateException("update_session_failed_${response.code()}")
            }
        } catch (e: Exception) {
            throw e
            // 服务器同步失败
        }
    }
    
    suspend fun startNewSession(): ChatSession {
        val token = requireAuthToken()
        val userId = authRepository.getUserId()
        
        // 将所有缓存会话设为非活跃
        cachedSessions.forEachIndexed { index, session ->
            if (session.isActive) {
                cachedSessions[index] = session.copy(isActive = false)
            }
        }
        currentActiveSessionId = null
        _messagesFlow.value = emptyList()
        
        // 同步到服务器
        try {
            val response = chatApiService.deactivateAllSessions(token)
            if (!response.isSuccessful) {
                throw IllegalStateException("deactivate_sessions_failed_${response.code()}")
            }
        } catch (e: Exception) {
            throw e
            // 服务器同步失败
        }
        
        // 创建新会话
        return createNewSession(userId)
    }
    
    suspend fun getAllSessions(): List<ChatSession> {
        val token = requireAuthToken()
        // 先返回缓存
        if (cachedSessions.isNotEmpty()) {
            return cachedSessions.toList()
        }
        
        // 从服务器获取
        try {
            val response = chatApiService.getSessions(token)
            if (response.isSuccessful && response.body() != null) {
                val userId = authRepository.getUserId()
                val sessions = response.body()!!.map { serverSession ->
                    ChatSession(
                        sessionId = serverSession.id,
                        userId = userId,
                        startTime = parseDate(serverSession.start_time),
                        endTime = serverSession.end_time?.let { parseDate(it) },
                        moodReport = serverSession.mood_report,
                        isActive = serverSession.is_active
                    )
                }.toMutableList()
                
                cachedSessions = sessions
                return sessions.toList()
            }
        } catch (e: Exception) {
            // 获取失败
        }
        
        return emptyList()
    }
    
    fun getMessageCountForSession(sessionId: String): Int {
        return cachedMessages[sessionId]?.size ?: 0
    }
    
    fun getFirstUserMessageForSession(sessionId: String): String? {
        return cachedMessages[sessionId]?.firstOrNull { it.isFromUser }?.content
    }
    
    suspend fun deleteSession(sessionId: String) {
        val token = requireAuthToken()
        // 从缓存删除
        cachedSessions.removeAll { it.sessionId == sessionId }
        cachedMessages.remove(sessionId)
        
        if (currentActiveSessionId == sessionId) {
            currentActiveSessionId = null
            _messagesFlow.value = emptyList()
        }
        
        // 从服务器删除
        try {
            val response = chatApiService.deleteSession(token, sessionId)
            if (!response.isSuccessful) {
                throw IllegalStateException("delete_session_failed_${response.code()}")
            }
        } catch (e: Exception) {
            throw e
            // 服务器删除失败
        }
    }
    
    suspend fun sendSessionToAdmin(sessionId: String): Result<String> {
        return try {
            val response = chatApiService.sendSessionToAdmin(requireAuthToken(), sessionId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!["message"] ?: "发送成功")
            } else {
                Result.failure(Exception("发送失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 清除缓存（登出时调用）
    fun clearCache() {
        cachedSessions.clear()
        cachedMessages.clear()
        currentActiveSessionId = null
        _messagesFlow.value = emptyList()
    }
}
