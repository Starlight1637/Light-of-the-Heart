package com.mindful.companion.ui.screens.report

import android.content.Context
import android.content.Intent

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mindful.companion.ui.components.AITypewriterText
import com.mindful.companion.ui.components.EmptyState
import com.mindful.companion.ui.components.HealingGlassCard
import com.mindful.companion.ui.components.ParticleBackground
import com.mindful.companion.ui.theme.*

@Composable
fun WeeklyReportScreen(
    navController: NavController,
    viewModel: WeeklyReportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(StitchSurface, StitchSurfaceContainerLow, StitchSurface)))
    ) {
        ParticleBackground(particleCount = 8)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = HealingSpacing.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(HealingSpacing.Medium)
        ) {
            item { Spacer(modifier = Modifier.height(HealingSpacing.Large)) }

            // 顶部栏
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回",
                            tint = StitchOnSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "心情周报",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = StitchOnSurface
                    )
                    if (uiState.week.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.week,
                            style = MaterialTheme.typography.bodySmall,
                            color = StitchOnSurfaceVariant
                        )
                    }
                }
            }

            when {
                uiState.isLoading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = StitchPrimaryContainer)
                        }
                    }
                }

                uiState.error != null -> {
                    item {
                        EmptyState(
                            title = "加载失败",
                            description = uiState.error ?: "",
                            actionText = "重试",
                            onActionClick = { viewModel.loadWeeklyReport() }
                        )
                    }
                }

                uiState.totalPosts == 0 -> {
                    item {
                        EmptyState(
                            title = "本周暂无记录",
                            description = "去写几篇日记，就能看到你的心情周报啦",
                            actionText = "写日记",
                            onActionClick = { navController.navigate("create") }
                        )
                    }
                }

                else -> {
                    // 英雄总结卡
                    item {
                        HealingGlassCard(modifier = Modifier.fillMaxWidth(), glassAlpha = 0.0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                StitchPrimaryContainer.copy(alpha = 0.7f),
                                                StitchPrimaryFixed.copy(alpha = 0.9f),
                                                StitchTertiaryFixed.copy(alpha = 0.8f)
                                            )
                                        )
                                    )
                            ) {
                                Column(modifier = Modifier.padding(HealingSpacing.CardPaddingLarge)) {
                                    Text(
                                        text = "本周心情",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = StitchPrimary.copy(alpha = 0.8f)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = uiState.dominantMood,
                                        style = MaterialTheme.typography.displaySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = StitchPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "共记录 ${uiState.totalPosts} 篇日记",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = StitchOnSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // 情绪趋势柱状图
                    item {
                        HealingGlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(HealingSpacing.CardPaddingLarge)) {
                                Text(
                                    text = "情绪趋势",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = StitchOnSurface
                                )
                                Spacer(modifier = Modifier.height(HealingSpacing.Medium))
                                val days = listOf("一", "二", "三", "四", "五", "六", "日")
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    val bars = if (uiState.dailyMoods.isNotEmpty())
                                        uiState.dailyMoods.map { it.avg_score }
                                    else List(7) { 0.5f }

                                    bars.forEachIndexed { i, score ->
                                        val animatedHeight by animateFloatAsState(
                                            targetValue = score.coerceIn(0f, 1f),
                                            animationSpec = tween(600, delayMillis = i * 80)
                                        )
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Bottom,
                                            modifier = Modifier.weight(1f).fillMaxHeight()
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .width(18.dp)
                                                    .height((animatedHeight * 80).dp.coerceAtLeast(4.dp))
                                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                                    .background(
                                                        Brush.verticalGradient(
                                                            listOf(
                                                                StitchPrimaryContainer,
                                                                StitchPrimaryFixed
                                                            )
                                                        )
                                                    )
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = days.getOrElse(i) { "" },
                                                style = MaterialTheme.typography.labelSmall,
                                                color = StitchOnSurfaceVariant,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 情绪分布饼图
                    item {
                        val dist = uiState.moodDistribution
                        if (dist != null) {
                            HealingGlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(HealingSpacing.CardPaddingLarge)) {
                                    Text(
                                        text = "情绪分布",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = StitchOnSurface
                                    )
                                    Spacer(modifier = Modifier.height(HealingSpacing.Medium))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val positiveAngle = dist.positive * 360f
                                        val neutralAngle = dist.neutral * 360f
                                        val negativeAngle = 1f - dist.positive - dist.neutral
                                        Canvas(modifier = Modifier.size(100.dp)) {
                                            val strokeWidth = 20.dp.toPx()
                                            val radius = (size.minDimension - strokeWidth) / 2f
                                            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                                            val arcSize = Size(radius * 2, radius * 2)

                                            var startAngle = -90f
                                            drawArc(
                                                color = Color(0xFF7EC8A3),
                                                startAngle = startAngle,
                                                sweepAngle = positiveAngle,
                                                useCenter = false,
                                                topLeft = topLeft,
                                                size = arcSize,
                                                style = Stroke(width = strokeWidth)
                                            )
                                            startAngle += positiveAngle
                                            drawArc(
                                                color = Color(0xFFFFD166),
                                                startAngle = startAngle,
                                                sweepAngle = neutralAngle,
                                                useCenter = false,
                                                topLeft = topLeft,
                                                size = arcSize,
                                                style = Stroke(width = strokeWidth)
                                            )
                                            startAngle += neutralAngle
                                            drawArc(
                                                color = Color(0xFFFF7043),
                                                startAngle = startAngle,
                                                sweepAngle = (negativeAngle * 360f).coerceAtLeast(0f),
                                                useCenter = false,
                                                topLeft = topLeft,
                                                size = arcSize,
                                                style = Stroke(width = strokeWidth)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(HealingSpacing.Large))
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            LegendItem(Color(0xFF7EC8A3), "愉悦  ${(dist.positive * 100).toInt()}%")
                                            LegendItem(Color(0xFFFFD166), "平静  ${(dist.neutral * 100).toInt()}%")
                                            LegendItem(Color(0xFFFF7043), "低落  ${((1f - dist.positive - dist.neutral) * 100).toInt()}%")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // AI 建议卡
                    item {
                        if (uiState.aiSummary.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(HealingShapes.ExtraLarge)
                                    .background(StitchSecondaryContainer.copy(alpha = 0.4f))
                                    .padding(HealingSpacing.CardPaddingLarge)
                            ) {
                                Column {
                                    Text(
                                        text = "AI 心情洞察",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = StitchPrimary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    AITypewriterText(
                                        text = uiState.aiSummary,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = StitchOnSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // 分享按钮
                    item {
                        Button(
                            onClick = { shareWeeklyReport(context, uiState) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StitchPrimaryContainer
                            )
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("分享本周心情", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(HealingSpacing.XXLarge)) }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(50))
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = StitchOnSurfaceVariant
        )
    }
}

private fun shareWeeklyReport(context: Context, uiState: WeeklyReportUiState) {
    val text = buildString {
        appendLine("心光心情周报")
        if (uiState.week.isNotBlank()) {
            appendLine("周期：${uiState.week}")
        }
        appendLine("本周记录：${uiState.totalPosts} 篇")
        appendLine("主要心情：${uiState.dominantMood}")
        if (uiState.aiSummary.isNotBlank()) {
            appendLine()
            appendLine(uiState.aiSummary)
        }
    }
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(sendIntent, "分享本周心情"))
}
