package com.mindful.companion.ui.screens.breathing

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mindful.companion.ui.theme.StitchPrimary
import com.mindful.companion.ui.theme.StitchPrimaryFixed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreathingScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("breathing_prefs", Context.MODE_PRIVATE)

    var isPlaying by remember { mutableStateOf(false) }
    var breathPhase by remember { mutableStateOf(BreathPhase.INHALE) }
    var selectedDuration by remember { mutableStateOf(BreathingDuration.THIRTY_SECONDS) }
    var remainingCycles by remember { mutableStateOf(0) }
    var showInstructions by remember { mutableStateOf(false) }

    // Animation progress (0f to 1f)
    val animationProgress = remember { Animatable(0f) }

    // Circle size derived from sin of animation progress
    val circleSize by remember {
        derivedStateOf {
            val baseSize = 100f
            val maxSize = 200f
            val progress = sin(animationProgress.value * PI).toFloat()
            baseSize + (maxSize - baseSize) * progress
        }
    }

    // Circle inner color animation — shifts between INHALE (warmer/brighter) and EXHALE (slightly cooler)
    val circleInnerColor by animateColorAsState(
        targetValue = when (breathPhase) {
            BreathPhase.INHALE -> Color(0xFFFFB07A)
            BreathPhase.EXHALE -> Color(0xFFE07030)
        },
        animationSpec = tween(2000),
        label = "circle_color"
    )

    // Breathing loop
    LaunchedEffect(isPlaying, selectedDuration) {
        if (isPlaying) {
            remainingCycles = selectedDuration.cycles

            while (isPlaying && remainingCycles > 0) {
                // Inhale — 4 seconds
                breathPhase = BreathPhase.INHALE
                animationProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(4000, easing = FastOutSlowInEasing)
                )

                if (!isPlaying) break

                // Exhale — 4 seconds
                breathPhase = BreathPhase.EXHALE
                animationProgress.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(4000, easing = FastOutSlowInEasing)
                )

                remainingCycles--

                if (remainingCycles == 0) {
                    isPlaying = false
                    breathPhase = BreathPhase.INHALE

                    // Save practice count for today
                    val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    val energyPrefs = context.getSharedPreferences("energy_prefs", Context.MODE_PRIVATE)
                    val count = energyPrefs.getInt("practice_$todayKey", 0)
                    energyPrefs.edit().putInt("practice_$todayKey", count + 1).apply()
                }
            }
        } else {
            // Reset animation when stopped
            animationProgress.snapTo(0f)
        }
    }

    // Instructions dialog
    if (showInstructions) {
        AlertDialog(
            onDismissRequest = { showInstructions = false },
            containerColor = Color(0xFF2A1208),
            title = {
                Text(
                    "呼吸练习说明",
                    color = Color.White
                )
            },
            text = {
                Column {
                    Text(
                        "跟随圆圈的节奏进行呼吸：\n",
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        "• 圆圈扩大时 - 吸气 4 秒",
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        "• 圆圈缩小时 - 呼气 4 秒\n",
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        "建议找一个安静舒适的地方，放松身心，专注于呼吸。",
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showInstructions = false }) {
                    Text("开始练习", color = StitchPrimaryFixed)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        prefs.edit().putBoolean("hide_instructions", true).apply()
                        showInstructions = false
                    }
                ) {
                    Text("不再提示", color = StitchPrimaryFixed)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF1E0C04),
                        Color(0xFF3A1A06),
                        Color(0xFF1E0C04)
                    )
                )
            )
    ) {
        // Back button
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "返回",
                tint = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Top status text
            if (isPlaying) {
                Text(
                    text = "剩余 $remainingCycles 个循环",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )
            } else {
                Text(
                    text = "深呼吸，放松身心",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Light,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Breathing animation circle — 4 layered Canvas
            Box(
                modifier = Modifier.size(320.dp),
                contentAlignment = Alignment.Center
            ) {
                // Layer 1: Outer halo
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFF6020).copy(alpha = 0.10f),
                                Color.Transparent
                            )
                        ),
                        radius = circleSize * 2.2f
                    )
                }

                // Layer 2: Middle glow
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFF7A30).copy(alpha = 0.20f),
                                Color.Transparent
                            )
                        ),
                        radius = circleSize * 1.5f
                    )
                }

                // Layer 3: Main circle — warm amber/coral gradient
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                circleInnerColor,
                                Color(0xFFD4603A),
                                StitchPrimary
                            )
                        ),
                        radius = circleSize
                    )
                }

                // Layer 4: Center highlight
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color.White,
                        alpha = 0.15f,
                        radius = circleSize * 0.35f
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Phase label
            Text(
                text = breathPhase.text,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Phase instruction
            Text(
                text = breathPhase.instruction,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Duration chips — only shown when not playing
            if (!isPlaying) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "选择练习时长",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        BreathingDuration.values().forEach { duration ->
                            FilterChip(
                                selected = selectedDuration == duration,
                                onClick = { selectedDuration = duration },
                                label = { Text(duration.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color.White.copy(alpha = 0.12f),
                                    labelColor = Color.White.copy(alpha = 0.7f),
                                    selectedContainerColor = Color.White,
                                    selectedLabelColor = StitchPrimary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = Color.White.copy(alpha = 0.2f),
                                    selectedBorderColor = Color.Transparent
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Play/Pause FAB
            FloatingActionButton(
                onClick = {
                    if (!isPlaying && !prefs.getBoolean("hide_instructions", false)) {
                        showInstructions = true
                    }
                    isPlaying = !isPlaying
                },
                modifier = Modifier.size(80.dp),
                containerColor = Color.White,
                contentColor = circleInnerColor
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "暂停" else "开始",
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

enum class BreathPhase(val text: String, val instruction: String) {
    INHALE("吸气", "缓慢深吸，感受空气充满肺部"),
    EXHALE("呼气", "慢慢呼出，释放所有紧张")
}

enum class BreathingDuration(val label: String, val cycles: Int) {
    FIFTEEN_SECONDS("15秒", 2),
    THIRTY_SECONDS("30秒", 4),
    ONE_MINUTE("1分钟", 8)
}
