package com.mindful.companion.ui.screens.whitenoise

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mindful.companion.ui.theme.StitchPrimary
import com.mindful.companion.ui.theme.StitchPrimaryContainer
import com.mindful.companion.ui.theme.StitchPrimaryFixed

@Composable
fun WhiteNoiseScreen(
    navController: NavController,
    viewModel: WhiteNoiseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentSound = uiState.currentSound
    val isPlaying = uiState.isPlaying
    val volume = uiState.volume
    val sounds = uiState.sounds

    val backgroundBrush = Brush.verticalGradient(
        listOf(
            Color(0xFF12080A),
            Color(0xFF1E0C10),
            Color(0xFF0E0608)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        // Back button — top start
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "返回",
                tint = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            // Page title
            Text(
                text = "白噪声",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "为心灵营造宁静空间",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Now playing card — only when a sound is selected
            if (currentSound != null) {
                NowPlayingCard(
                    sound = currentSound,
                    isPlaying = isPlaying,
                    volume = volume,
                    onPlayPause = { viewModel.togglePlayPause() },
                    onStop = { viewModel.stopSound() },
                    onVolumeChange = { viewModel.setVolume(it) }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Section label
            Text(
                text = "选择声音",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Sound tiles
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                sounds.forEach { sound ->
                    SoundTile(
                        sound = sound,
                        isActive = currentSound == sound && isPlaying,
                        onClick = { viewModel.playSound(sound) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun NowPlayingCard(
    sound: WhiteNoiseSound,
    isPlaying: Boolean,
    volume: Float,
    onPlayPause: () -> Unit,
    onStop: () -> Unit,
    onVolumeChange: (Float) -> Unit
) {
    val cardBrush = Brush.linearGradient(
        listOf(
            Color(0xFF2A1010),
            Color(0xFF1A0808)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardBrush)
            .border(
                width = 1.dp,
                color = StitchPrimary.copy(alpha = 0.4f),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Icon + name + play/pause
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            color = StitchPrimary.copy(alpha = 0.25f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = sound.icon,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = StitchPrimaryFixed
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sound.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (isPlaying) "正在播放" else "已暂停",
                        style = MaterialTheme.typography.bodySmall,
                        color = StitchPrimaryFixed.copy(alpha = 0.8f)
                    )
                }

                IconButton(onClick = onPlayPause) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "暂停" else "播放",
                        modifier = Modifier.size(32.dp),
                        tint = StitchPrimaryFixed
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Volume row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.VolumeDown,
                    contentDescription = "最小音量",
                    modifier = Modifier.size(18.dp),
                    tint = Color.White.copy(alpha = 0.4f)
                )
                Slider(
                    value = volume,
                    onValueChange = onVolumeChange,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = StitchPrimaryFixed,
                        activeTrackColor = StitchPrimary,
                        inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                    )
                )
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "最大音量",
                    modifier = Modifier.size(18.dp),
                    tint = Color.White.copy(alpha = 0.4f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Stop button
            OutlinedButton(
                onClick = onStop,
                modifier = Modifier.fillMaxWidth(),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.2f)
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "停止播放")
            }
        }
    }
}

@Composable
private fun SoundTile(
    sound: WhiteNoiseSound,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val soundBgColor = when (sound.id) {
        "rain"    -> Color(0xFF0E2535)
        "birds"   -> Color(0xFF0D2015)
        "thunder" -> Color(0xFF1A0D30)
        else      -> Color(0xFF1A1010)
    }

    val iconBg = when (sound.id) {
        "rain"    -> Color(0xFF1A3A50)
        "birds"   -> Color(0xFF1A3520)
        "thunder" -> Color(0xFF2A1A45)
        else      -> Color(0xFF2A1A1A)
    }

    val iconTint = when (sound.id) {
        "rain"    -> Color(0xFF7BBEDD)
        "birds"   -> Color(0xFF7BC47A)
        "thunder" -> Color(0xFFAA88DD)
        else      -> Color.White.copy(alpha = 0.6f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(soundBgColor)
            .then(
                if (isActive)
                    Modifier.border(
                        width = 2.dp,
                        color = StitchPrimary.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(16.dp)
                    )
                else
                    Modifier
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = iconBg,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = sound.icon,
                    contentDescription = null,
                    modifier = Modifier.size(26.dp),
                    tint = iconTint
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sound.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    text = sound.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }

            if (isActive) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "播放中",
                    modifier = Modifier.size(20.dp),
                    tint = StitchPrimaryFixed
                )
            }
        }
    }
}
