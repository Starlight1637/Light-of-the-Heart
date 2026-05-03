package com.mindful.companion.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.mindful.companion.ui.theme.HealingAnimations
import com.mindful.companion.ui.theme.ParticleColors
import kotlin.math.sin
import kotlin.random.Random

// ============================================================
// 漂浮粒子背景
// 代表情绪的轻盈与流动
// ============================================================

private data class Particle(
    val startX: Float,      // 初始 x 位置 (0..1)
    val startY: Float,      // 初始 y 位置 (0..1)
    val size: Float,        // 粒子半径 dp
    val speed: Float,       // 运动速度 (0..1)
    val phaseOffset: Float, // sin波相位偏移
    val alpha: Float,       // 透明度
    val color: Color,
    val lifetime: Float     // 生命周期偏移 (0..1)，错开粒子位置
)

@Composable
fun ParticleBackground(
    modifier: Modifier = Modifier,
    particleCount: Int = HealingAnimations.Particle.Count
) {
    val particles = remember {
        List(particleCount) {
            Particle(
                startX = Random.nextFloat(),
                startY = Random.nextFloat(),
                size = Random.nextFloat() *
                        (HealingAnimations.Particle.MaxSizeDp - HealingAnimations.Particle.MinSizeDp) +
                        HealingAnimations.Particle.MinSizeDp,
                speed = Random.nextFloat() *
                        (HealingAnimations.Particle.SpeedMax - HealingAnimations.Particle.SpeedMin) +
                        HealingAnimations.Particle.SpeedMin,
                phaseOffset = Random.nextFloat() * 6.28f,  // 0 ~ 2π
                alpha = Random.nextFloat() *
                        (HealingAnimations.Particle.AlphaMax - HealingAnimations.Particle.AlphaMin) +
                        HealingAnimations.Particle.AlphaMin,
                color = ParticleColors.random(),
                lifetime = Random.nextFloat()
            )
        }
    }

    // 全局时间驱动器 (0→1 循环，周期 8000ms)
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_time"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        particles.forEach { p ->
            // 粒子纵向位置: 从底部往上漂浮，循环
            val t = (time * p.speed + p.lifetime) % 1f
            val y = h * (1f - t)    // 从底部向顶部

            // 粒子横向: sin 波动
            val waveAmp = w * 0.04f
            val x = w * p.startX + waveAmp * sin(t * 6.28f * 2 + p.phaseOffset)

            // 边界外不绘制
            if (x < 0 || x > w || y < 0 || y > h) return@forEach

            // 淡入淡出（接近顶部或底部时透明）
            val edgeFade = when {
                t < 0.1f -> t / 0.1f
                t > 0.9f -> (1f - t) / 0.1f
                else -> 1f
            }

            drawCircle(
                color = p.color.copy(alpha = p.alpha * edgeFade),
                radius = p.size * density,
                center = Offset(x, y)
            )
        }
    }
}
