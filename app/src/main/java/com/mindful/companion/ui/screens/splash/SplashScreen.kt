package com.mindful.companion.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindful.companion.ui.components.ParticleBackground
import com.mindful.companion.ui.theme.GoldenHour
import com.mindful.companion.ui.theme.HealingAnimations
import com.mindful.companion.ui.theme.HealingTeal
import com.mindful.companion.ui.theme.MorningPink
import com.mindful.companion.ui.theme.SoftLavender
import com.mindful.companion.ui.theme.SoftPeach
import com.mindful.companion.ui.theme.WarmCoral
import com.mindful.companion.ui.theme.WarmCream
import com.mindful.companion.ui.theme.WarmTextDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onTimeout: () -> Unit) {

    // --- 动画值 ---
    val glowScale = remember { Animatable(0f) }
    val glowAlpha = remember { Animatable(0f) }
    val logoAlpha = remember { Animatable(0f) }
    val logoTransY = remember { Animatable(40f) }
    val taglineAlpha = remember { Animatable(0f) }

    // 呼吸光晕（无限循环）
    val breath = rememberInfiniteTransition(label = "breath")
    val breathScale by breath.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath_scale"
    )

    // 旋转光圈
    val rotation by breath.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_rotate"
    )

    // --- 时序编排 ---
    LaunchedEffect(Unit) {
        launch {
            // 光晕扩散
            glowAlpha.animateTo(1f, tween(600))
            glowScale.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
        }
        // Logo 淡入上移
        delay(400L)
        launch {
            logoAlpha.animateTo(1f, tween(700))
            logoTransY.animateTo(0f, tween(700, easing = FastOutSlowInEasing))
        }
        // 标语淡入
        delay(800L)
        taglineAlpha.animateTo(1f, tween(600))

        // 停留后跳转
        delay(1600L)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        SoftPeach,
                        WarmCream,
                        WarmCream
                    ),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // 粒子背景
        ParticleBackground(particleCount = 15)

        // 光晕圆圈
        Box(
            modifier = Modifier
                .size(320.dp)
                .scale(glowScale.value * breathScale)
                .graphicsLayer {
                    alpha = glowAlpha.value * 0.55f
                    rotationZ = rotation
                }
                .background(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            HealingTeal.copy(alpha = 0.3f),
                            MorningPink.copy(alpha = 0.3f),
                            SoftLavender.copy(alpha = 0.3f),
                            GoldenHour.copy(alpha = 0.3f),
                            HealingTeal.copy(alpha = 0.3f)
                        )
                    ),
                    shape = androidx.compose.foundation.shape.CircleShape
                )
        )

        // Logo + 标语
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // App 名称
            Text(
                text = "心光",
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = WarmTextDark,
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.graphicsLayer {
                    alpha = logoAlpha.value
                    translationY = logoTransY.value
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 副标题
            Text(
                text = "你不孤单，我们一直都在",
                style = MaterialTheme.typography.bodyLarge,
                color = WarmTextDark.copy(alpha = 0.7f),
                modifier = Modifier.graphicsLayer {
                    alpha = taglineAlpha.value
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 英文副标题
            Text(
                text = "Mindful Companion",
                style = MaterialTheme.typography.labelMedium,
                color = HealingTeal.copy(alpha = taglineAlpha.value * 0.8f)
            )
        }
    }
}
