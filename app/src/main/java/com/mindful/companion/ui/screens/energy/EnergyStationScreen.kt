package com.mindful.companion.ui.screens.energy

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mindful.companion.ui.components.HealingGlassCard
import com.mindful.companion.ui.components.ParticleBackground
import com.mindful.companion.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ============================================================
// 数据类：精选卡片内容（基于时段）
// ============================================================
private data class FeaturedContent(
    val title: String,
    val subtitle: String,
    val route: String,
    val timeTag: String
)

private fun getFeaturedContent(hour: Int): FeaturedContent {
    return when (hour) {
        in 6..11 -> FeaturedContent(
            title = "晨间呼吸练习",
            subtitle = "深呼吸，开启元气满满的一天",
            route = "breathing",
            timeTag = "早上"
        )
        in 12..17 -> FeaturedContent(
            title = "午后放松",
            subtitle = "闭目养神，恢复专注力",
            route = "whitenoise",
            timeTag = "下午"
        )
        else -> FeaturedContent(
            title = "睡前助眠白噪声",
            subtitle = "平缓心绪，进入深度睡眠",
            route = "whitenoise",
            timeTag = "晚上"
        )
    }
}

// ============================================================
// 分类卡数据
// ============================================================
private data class CategoryItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val bgColor: Color,
    val route: String
)

@Composable
fun EnergyStationScreen(navController: NavController) {
    val context = LocalContext.current

    // 时段
    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val featured = remember(currentHour) { getFeaturedContent(currentHour) }

    // 今日练习次数（SharedPreferences）
    val todayKey = remember {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        "practice_" + sdf.format(Date())
    }
    val practiceCount = remember {
        val prefs = context.getSharedPreferences("energy_prefs", Context.MODE_PRIVATE)
        prefs.getInt(todayKey, 0)
    }

    // 分类数据
    val categories = remember {
        listOf(
            CategoryItem(
                icon = Icons.Outlined.SelfImprovement,
                title = "冥想引导",
                subtitle = "4-7-8 呼吸法",
                bgColor = StitchPrimaryFixed,
                route = "breathing"
            ),
            CategoryItem(
                icon = Icons.Outlined.MusicNote,
                title = "白噪声",
                subtitle = "雨声 · 鸟鸣 · 雷声",
                bgColor = StitchSecondaryContainer,
                route = "whitenoise"
            ),
            CategoryItem(
                icon = Icons.Outlined.FormatQuote,
                title = "每日一句",
                subtitle = "温暖心灵",
                bgColor = StitchTertiaryFixed,
                route = "quotes"
            )
        )
    }

    // 交错动画状态（6个主要区块）
    val blockVisible = remember { mutableStateListOf<Boolean>().apply { repeat(6) { add(false) } } }
    LaunchedEffect(Unit) {
        blockVisible.indices.forEach { i ->
            delay(i * 80L + 80L)
            blockVisible[i] = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(StitchSurface, StitchSurfaceContainerLow, StitchSurface))
            )
    ) {
        ParticleBackground(particleCount = 10)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = HealingSpacing.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(HealingSpacing.Medium)
        ) {
            // 0: 顶部间距
            item { Spacer(modifier = Modifier.height(HealingSpacing.Large)) }

            // blockVisible[0]: Hero 标题
            item {
                AnimatedVisibility(
                    visible = blockVisible.getOrElse(0) { false },
                    enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 4 }
                ) {
                    Column {
                        Text(
                            text = "心光 · 宁静",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = StitchOnSurface
                        )
                        Text(
                            text = "为心灵找一片安静之地",
                            style = MaterialTheme.typography.bodyMedium,
                            color = StitchOnSurfaceVariant
                        )
                    }
                }
            }

            // blockVisible[1]: 今日练习次数徽章（count > 0 时显示内容，count == 0 时仍占位）
            item {
                AnimatedVisibility(
                    visible = blockVisible.getOrElse(1) { false },
                    enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 4 }
                ) {
                    if (practiceCount > 0) {
                        HealingGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            glassAlpha = 0.65f
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = HealingSpacing.CardPadding, vertical = HealingSpacing.Small),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(HealingSpacing.XSmall)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = StitchPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "今日已完成 $practiceCount 次练习 ✓",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = StitchPrimary
                                )
                            }
                        }
                    } else {
                        // count == 0 时不显示内容，但保留占位（高度为0的Box）
                        Box(modifier = Modifier.height(0.dp))
                    }
                }
            }

            // blockVisible[2]: 精选大卡（基于时段）
            item {
                AnimatedVisibility(
                    visible = blockVisible.getOrElse(2) { false },
                    enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 4 }
                ) {
                    HealingGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        onClick = { navController.navigate(featured.route) },
                        glassAlpha = 0.0f
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            StitchPrimary.copy(alpha = 0.75f),
                                            StitchPrimaryContainer.copy(alpha = 0.5f),
                                            StitchPrimaryFixed
                                        )
                                    )
                                )
                        ) {
                            // 时段标签（TopStart 药丸形）
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(HealingSpacing.Medium),
                                shape = RoundedCornerShape(50),
                                color = Color.White.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = featured.timeTag,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            // 标题 + 副标题（BottomStart）
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(HealingSpacing.CardPadding)
                            ) {
                                Text(
                                    text = featured.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = featured.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }

                            // 播放按钮（BottomEnd）
                            IconButton(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(HealingSpacing.Medium),
                                onClick = { navController.navigate(featured.route) }
                            ) {
                                Icon(
                                    Icons.Outlined.PlayCircle,
                                    contentDescription = "播放",
                                    tint = Color.White,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                    }
                }
            }

            // blockVisible[3]: 分类横向滚动列表
            item {
                AnimatedVisibility(
                    visible = blockVisible.getOrElse(3) { false },
                    enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 4 }
                ) {
                    Column {
                        Text(
                            text = "分类",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = StitchOnSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(HealingSpacing.Small),
                            contentPadding = PaddingValues(horizontal = 0.dp)
                        ) {
                            items(categories) { item ->
                                CategoryCard(
                                    icon = item.icon,
                                    title = item.title,
                                    subtitle = item.subtitle,
                                    bgColor = item.bgColor,
                                    onClick = { navController.navigate(item.route) }
                                )
                            }
                        }
                    }
                }
            }

            // blockVisible[4]: 快速放松横条
            item {
                AnimatedVisibility(
                    visible = blockVisible.getOrElse(4) { false },
                    enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 4 }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(HealingShapes.ExtraLarge)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        StitchPrimaryFixed.copy(alpha = 0.9f),
                                        StitchTertiaryFixed.copy(alpha = 0.8f),
                                        StitchSecondaryContainer.copy(alpha = 0.6f)
                                    )
                                )
                            )
                            .padding(HealingSpacing.CardPaddingLarge)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "快速放松",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = StitchPrimary
                                )
                                Text(
                                    text = "5 分钟内平复焦虑",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StitchOnSurfaceVariant
                                )
                            }
                            Button(
                                onClick = { navController.navigate("breathing") },
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(containerColor = StitchPrimary)
                            ) {
                                Text("开始练习", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // 底部间距
            item { Spacer(modifier = Modifier.height(HealingSpacing.XXLarge)) }
        }
    }
}

// ============================================================
// 分类卡片（横向列表用）
// ============================================================
@Composable
private fun CategoryCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    bgColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(130.dp)
            .height(110.dp)
            .clip(HealingShapes.Large)
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(HealingSpacing.Medium)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = StitchOnSurface.copy(alpha = 0.75f),
            modifier = Modifier
                .size(26.dp)
                .align(Alignment.TopStart)
        )
        Column(
            modifier = Modifier.align(Alignment.BottomStart)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = StitchOnSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = StitchOnSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
