package com.mindful.companion.ui.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mindful.companion.ui.components.HealingGlassCard
import com.mindful.companion.ui.components.ParticleBackground
import com.mindful.companion.ui.theme.*

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(StitchSurface, StitchSurfaceContainerLow, StitchSurface))
            )
    ) {
        ParticleBackground(particleCount = 8)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = HealingSpacing.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(HealingSpacing.Medium)
        ) {
            item { Spacer(modifier = Modifier.height(HealingSpacing.Large)) }

            // TopAppBar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = uiState.greetingText,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = StitchOnSurface
                        )
                        Text(
                            text = "今天感觉如何？",
                            style = MaterialTheme.typography.bodyMedium,
                            color = StitchOnSurfaceVariant
                        )
                    }
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "通知",
                            tint = StitchOnSurfaceVariant
                        )
                    }
                }
            }

            // 问候大卡
            item {
                HealingGlassCard(modifier = Modifier.fillMaxWidth(), glassAlpha = 0.0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        StitchPrimaryFixed.copy(alpha = 0.9f),
                                        StitchTertiaryFixed.copy(alpha = 0.8f)
                                    )
                                )
                            )
                    ) {
                        Column(modifier = Modifier.padding(HealingSpacing.CardPaddingLarge)) {
                            Text(
                                text = "记录此刻的心情",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = StitchPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "写下今天的故事，AI 会陪伴你",
                                style = MaterialTheme.typography.bodySmall,
                                color = StitchOnSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(HealingSpacing.Medium))
                            Row(horizontalArrangement = Arrangement.spacedBy(HealingSpacing.Small)) {
                                Button(
                                    onClick = { navController.navigate("create") },
                                    shape = RoundedCornerShape(50),
                                    colors = ButtonDefaults.buttonColors(containerColor = StitchPrimary)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("记录此刻", fontSize = 13.sp)
                                }
                                OutlinedButton(
                                    onClick = { navController.navigate("energy") },
                                    shape = RoundedCornerShape(50),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StitchPrimary)
                                ) {
                                    Text("随便看看", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Bento 网格
            item {
                Column(verticalArrangement = Arrangement.spacedBy(HealingSpacing.Small)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(HealingSpacing.Small)) {
                        BentoCard(
                            title = "写日记",
                            subtitle = "记录心情",
                            bgColor = StitchPrimaryFixed,
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate("create") }
                        )
                        BentoCard(
                            title = "和AI倾诉",
                            subtitle = "随时陪伴",
                            bgColor = StitchSecondaryContainer,
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate("chat") }
                        )
                    }
                    BentoCardWide(
                        title = "放松时光",
                        subtitle = "呼吸 · 冥想 · 白噪声",
                        bgColor = StitchTertiaryFixed,
                        onClick = { navController.navigate("energy") }
                    )
                }
            }

            // 情绪周报预览
            item {
                HealingGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { navController.navigate("weekly_report") }
                ) {
                    Column(modifier = Modifier.padding(HealingSpacing.CardPadding)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "本周心情",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = StitchOnSurface
                            )
                            Text(
                                text = "查看完整版 →",
                                style = MaterialTheme.typography.labelMedium,
                                color = StitchPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        // 7根柱状图
                        val days = listOf("一", "二", "三", "四", "五", "六", "日")
                        Row(
                            modifier = Modifier.fillMaxWidth().height(60.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            uiState.weeklyMoodBars.forEachIndexed { i, score ->
                                val animated by animateFloatAsState(
                                    targetValue = score.coerceIn(0f, 1f),
                                    animationSpec = tween(500, delayMillis = i * 60)
                                )
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier.weight(1f).fillMaxHeight()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(12.dp)
                                            .height((animated * 40).dp.coerceAtLeast(3.dp))
                                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(StitchPrimaryContainer, StitchPrimaryFixed)
                                                )
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = days.getOrElse(i) { "" },
                                        fontSize = 9.sp,
                                        color = StitchOnSurfaceVariant
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "主导情绪：${uiState.dominantMood}",
                            style = MaterialTheme.typography.bodySmall,
                            color = StitchOnSurfaceVariant
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(HealingSpacing.XXLarge)) }
        }
    }
}

@Composable
private fun BentoCard(
    title: String,
    subtitle: String,
    bgColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(100.dp)
            .clip(HealingShapes.Large)
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(HealingSpacing.Medium)
    ) {
        Column(modifier = Modifier.align(Alignment.BottomStart)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = StitchOnSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = StitchOnSurfaceVariant
            )
        }
    }
}

@Composable
private fun BentoCardWide(
    title: String,
    subtitle: String,
    bgColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(HealingShapes.Large)
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(HealingSpacing.Medium)
    ) {
        Column(modifier = Modifier.align(Alignment.CenterStart)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = StitchOnSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = StitchOnSurfaceVariant
            )
        }
    }
}
