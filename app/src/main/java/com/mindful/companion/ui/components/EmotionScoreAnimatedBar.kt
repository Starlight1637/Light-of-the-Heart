package com.mindful.companion.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mindful.companion.ui.theme.HealingShapes
import com.mindful.companion.ui.theme.HealingSpacing
import com.mindful.companion.ui.theme.MorningPink
import com.mindful.companion.ui.theme.RiskCritical
import com.mindful.companion.ui.theme.RiskHigh
import com.mindful.companion.ui.theme.RiskLow
import com.mindful.companion.ui.theme.RiskMedium
import com.mindful.companion.ui.theme.SunriseOrange
import com.mindful.companion.ui.theme.WarmCoral
import com.mindful.companion.ui.theme.WarmTextLight

// ============================================================
// 情绪分数动画进度条
// Spring 弹性动画，分数从 0 弹入目标值
// ============================================================

@Composable
fun EmotionScoreAnimatedBar(
    score: Float,           // 0f ~ 1f
    modifier: Modifier = Modifier,
    label: String = "情绪指数",
    height: Dp = 8.dp,
    showLabel: Boolean = true
) {
    var targetScore by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(score) {
        targetScore = score
    }

    val animatedProgress by animateFloatAsState(
        targetValue = targetScore,
        animationSpec = spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "emotion_score"
    )

    val barGradient = when {
        score < 0.3f -> Brush.horizontalGradient(listOf(RiskCritical, RiskHigh))
        score < 0.5f -> Brush.horizontalGradient(listOf(RiskHigh, RiskMedium))
        score < 0.7f -> Brush.horizontalGradient(listOf(RiskMedium, SunriseOrange))
        else -> Brush.horizontalGradient(listOf(WarmCoral, RiskLow))
    }

    val emoji = when {
        score < 0.3f -> "😢"
        score < 0.5f -> "😕"
        score < 0.7f -> "😊"
        else -> "😄"
    }

    Column(modifier = modifier) {
        if (showLabel) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = WarmTextLight
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = emoji,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${(score * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.height(HealingSpacing.XXSmall))
        }

        // 背景轨道
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(HealingShapes.Full)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            // 进度条
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = animatedProgress.coerceIn(0f, 1f))
                    .height(height)
                    .clip(HealingShapes.Full)
                    .background(brush = barGradient)
            )
        }
    }
}
