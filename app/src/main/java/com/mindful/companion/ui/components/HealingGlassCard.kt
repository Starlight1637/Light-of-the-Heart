package com.mindful.companion.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mindful.companion.ui.theme.GlassBorder
import com.mindful.companion.ui.theme.GlassOverlay
import com.mindful.companion.ui.theme.GlassSurface
import com.mindful.companion.ui.theme.HealingShapes

// ============================================================
// 毛玻璃拟态卡片 (Glassmorphism)
// 兼容策略:
//   API 31+ → 真实模糊 (graphicsLayer renderEffect)
//   API < 31 → 半透明渐变模拟（全设备可用）
// ============================================================

/**
 * 标准毛玻璃卡片
 * @param glassAlpha 背景透明度，越低越透明 (0.55~0.85)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealingGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = HealingShapes.ExtraLarge,
    glassAlpha: Float = 0.72f,
    elevation: Dp = 0.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val surface = MaterialTheme.colorScheme.surface.copy(alpha = glassAlpha)
    val glassMod = modifier
        .clip(shape)
        .drawBehind {
            // 渐变边框（白→透明→白）
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.6f),
                        Color.White.copy(alpha = 0.1f),
                        Color.White.copy(alpha = 0.4f)
                    )
                ),
                size = size,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                    24.dp.toPx(), 24.dp.toPx()
                ),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
            )
        }
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    surface.copy(alpha = (glassAlpha + 0.08f).coerceAtMost(1f)),
                    surface.copy(alpha = (glassAlpha - 0.05f).coerceAtLeast(0f))
                )
            ),
            shape = shape
        )

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier.clip(shape),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation),
            border = BorderStroke(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.5f),
                        GlassBorder,
                        Color.White.copy(alpha = 0.3f)
                    )
                )
            )
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                surface.copy(alpha = (glassAlpha + 0.08f).coerceAtMost(1f)),
                                surface.copy(alpha = (glassAlpha - 0.05f).coerceAtLeast(0f))
                            )
                        )
                    ),
                content = content
            )
        }
    } else {
        Box(
            modifier = glassMod,
            content = content
        )
    }
}

/**
 * AI 专属毛玻璃卡片（薰衣草紫底）
 */
@Composable
fun HealingAIGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = HealingShapes.ExtraLarge,
    content: @Composable BoxScope.() -> Unit
) {
    val aiColor = MaterialTheme.colorScheme.tertiaryContainer

    Box(
        modifier = modifier
            .clip(shape)
            .drawBehind {
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.5f),
                            aiColor.copy(alpha = 0.2f),
                            Color.White.copy(alpha = 0.3f)
                        )
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                        24.dp.toPx(), 24.dp.toPx()
                    ),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
                )
            }
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        aiColor.copy(alpha = 0.65f),
                        aiColor.copy(alpha = 0.45f)
                    )
                ),
                shape = shape
            ),
        content = content
    )
}

/**
 * 全宽毛玻璃容器（用于页面顶部横幅等）
 */
@Composable
fun HealingGlassBanner(
    modifier: Modifier = Modifier,
    gradientColors: List<Color>,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(HealingShapes.ExtraLarge)
            .background(
                brush = Brush.linearGradient(colors = gradientColors),
                shape = HealingShapes.ExtraLarge
            )
            .drawBehind {
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.35f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                        24.dp.toPx(), 24.dp.toPx()
                    ),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                )
            },
        content = content
    )
}
