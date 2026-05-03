package com.mindful.companion.utils

import android.content.Context
import android.media.MediaPlayer
import android.os.PowerManager
import com.mindful.companion.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 白噪声播放器
 * 使用 MediaPlayer 播放真实的音频文件
 */
@Singleton
class WhiteNoiseGenerator @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private var mediaPlayer: MediaPlayer? = null
    private var currentType: SoundType? = null
    
    fun startSound(type: SoundType, volume: Float = 0.7f) {
        stopSound()
        
        val resId = when (type) {
            SoundType.RAIN -> R.raw.rain
            SoundType.BIRDS -> R.raw.birds
            SoundType.THUNDER -> R.raw.thunder
        }
        
        try {
            mediaPlayer = MediaPlayer.create(context, resId).apply {
                isLooping = true
                setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
                setVolume(volume, volume)
                setOnPreparedListener { it.start() }
            }
            currentType = type
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun stopSound() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
        currentType = null
    }
    
    fun pauseSound() {
        mediaPlayer?.pause()
    }
    
    fun resumeSound() {
        mediaPlayer?.start()
    }
    
    fun setVolume(volume: Float) {
        mediaPlayer?.setVolume(volume, volume)
    }
    
    fun isPlaying(): Boolean = mediaPlayer?.isPlaying == true
    
    fun release() {
        stopSound()
    }
}

enum class SoundType {
    RAIN,
    BIRDS,
    THUNDER
}
