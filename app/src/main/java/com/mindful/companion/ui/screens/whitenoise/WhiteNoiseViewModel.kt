package com.mindful.companion.ui.screens.whitenoise

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import com.mindful.companion.utils.SoundType
import com.mindful.companion.utils.WhiteNoiseGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class WhiteNoiseViewModel @Inject constructor(
    private val generator: WhiteNoiseGenerator
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(WhiteNoiseUiState())
    val uiState: StateFlow<WhiteNoiseUiState> = _uiState.asStateFlow()
    
    fun playSound(sound: WhiteNoiseSound) {
        try {
            generator.startSound(sound.soundType, _uiState.value.volume)
            
            _uiState.update { 
                it.copy(
                    currentSound = sound,
                    isPlaying = true
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.update { it.copy(isPlaying = false) }
        }
    }
    
    fun togglePlayPause() {
        if (_uiState.value.currentSound == null) return
        
        if (_uiState.value.isPlaying) {
            generator.pauseSound()
            _uiState.update { it.copy(isPlaying = false) }
        } else {
            generator.resumeSound()
            _uiState.update { it.copy(isPlaying = true) }
        }
    }
    
    fun stopSound() {
        generator.stopSound()
        _uiState.update { 
            it.copy(
                currentSound = null,
                isPlaying = false
            )
        }
    }
    
    fun setVolume(volume: Float) {
        generator.setVolume(volume)
        _uiState.update { it.copy(volume = volume) }
    }
    
    override fun onCleared() {
        super.onCleared()
        generator.release()
    }
}

data class WhiteNoiseUiState(
    val sounds: List<WhiteNoiseSound> = getDefaultSounds(),
    val currentSound: WhiteNoiseSound? = null,
    val isPlaying: Boolean = false,
    val volume: Float = 0.7f
)

data class WhiteNoiseSound(
    val id: String,
    val name: String,
    val description: String,
    val icon: ImageVector,
    val soundType: SoundType
)

private fun getDefaultSounds(): List<WhiteNoiseSound> {
    return listOf(
        WhiteNoiseSound(
            id = "rain",
            name = "雨声",
            description = "舒缓的雨声，帮助放松和睡眠",
            icon = Icons.Default.Cloud,
            soundType = SoundType.RAIN
        ),
        WhiteNoiseSound(
            id = "birds",
            name = "鸟鸣",
            description = "清晨的鸟鸣声，带来活力",
            icon = Icons.Default.Pets,
            soundType = SoundType.BIRDS
        ),
        WhiteNoiseSound(
            id = "thunder",
            name = "雷声",
            description = "远处的雷声，深度放松",
            icon = Icons.Default.Bolt,
            soundType = SoundType.THUNDER
        )
    )
}
