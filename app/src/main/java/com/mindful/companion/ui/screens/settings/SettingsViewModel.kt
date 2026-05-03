package com.mindful.companion.ui.screens.settings

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: com.mindful.companion.data.repository.AuthRepository,
    private val chatRepository: com.mindful.companion.data.repository.ChatRepository
) : ViewModel() {
    
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
    }
    
    fun isLoggedIn(): Boolean = authRepository.isLoggedIn()
    
    fun showLogoutDialog() {
        _uiState.update { it.copy(showLogoutDialog = true) }
    }
    
    fun dismissLogoutDialog() {
        _uiState.update { it.copy(showLogoutDialog = false) }
    }
    
    fun logout() {
        // 清除聊天缓存
        chatRepository.clearCache()
        // 清除认证信息
        authRepository.logout()
        dismissLogoutDialog()
    }
    
    private fun loadSettings() {
        val enabled = prefs.getBoolean("reminder_enabled", false)
        val time = prefs.getString("reminder_time", "09:00") ?: "09:00"
        
        _uiState.update {
            it.copy(
                reminderEnabled = enabled,
                reminderTime = time
            )
        }
    }
    
    fun setReminderTime(hour: Int, minute: Int) {
        val timeString = String.format("%02d:%02d", hour, minute)
        
        prefs.edit().apply {
            putBoolean("reminder_enabled", true)
            putString("reminder_time", timeString)
            apply()
        }
        
        _uiState.update {
            it.copy(
                reminderEnabled = true,
                reminderTime = timeString
            )
        }
        
        scheduleReminder(hour, minute)
    }
    
    fun disableReminder() {
        prefs.edit().apply {
            putBoolean("reminder_enabled", false)
            apply()
        }
        
        _uiState.update {
            it.copy(reminderEnabled = false)
        }
        
        cancelReminder()
    }
    
    private fun scheduleReminder(hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            
            // 如果设置的时间已经过了，就设置到明天
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        
        // 设置每天重复的闹钟
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }
    
    private fun cancelReminder() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
    }
}

data class SettingsUiState(
    val reminderEnabled: Boolean = false,
    val reminderTime: String = "09:00",
    val showLogoutDialog: Boolean = false
)
