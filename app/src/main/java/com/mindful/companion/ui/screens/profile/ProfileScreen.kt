package com.mindful.companion.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import com.mindful.companion.ui.theme.StitchPrimary
import com.mindful.companion.ui.theme.StitchPrimaryFixed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mindful.companion.ui.components.HealingGlassBanner
import com.mindful.companion.ui.components.HealingGlassCard
import com.mindful.companion.ui.theme.*

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val isLoggedIn = viewModel.isLoggedIn()
    val userInfo = viewModel.getUserInfo()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(StitchSurface, StitchSurfaceContainerLow, StitchSurface)))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = HealingSpacing.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(HealingSpacing.Medium)
        ) {
            item { Spacer(modifier = Modifier.height(HealingSpacing.Large)) }

            item {
                Text(
                    text = "个人中心",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = StitchOnSurface
                )
            }

            // 用户信息卡片
            item {
                HealingGlassBanner(
                    gradientColors = listOf(
                        StitchPrimaryContainer.copy(alpha = 0.6f),
                        StitchSecondaryContainer.copy(alpha = 0.6f),
                        StitchTertiaryFixed.copy(alpha = 0.8f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(HealingSpacing.CardPaddingLarge),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(HealingSpacing.AvatarLarge)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.85f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = StitchPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(HealingSpacing.Medium))
                        Column {
                            Text(
                                text = if (isLoggedIn) userInfo.account else "匿名用户",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = StitchOnSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isLoggedIn) userInfo.school else "你的隐私受到完全保护",
                                style = MaterialTheme.typography.bodyMedium,
                                color = StitchOnSurface.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }

            // 功能菜单
            item {
                HealingGlassCard(modifier = Modifier.fillMaxWidth(), glassAlpha = 0.75f) {
                    Column {
                        ProfileMenuItem(
                            icon = Icons.Outlined.BarChart,
                            title = "情绪周报",
                            subtitle = "查看本周心情趋势与AI洞察",
                            iconColor = StitchPrimary,
                            iconBg = StitchPrimaryFixed.copy(alpha = 0.5f),
                            onClick = { navController.navigate("weekly_report") }
                        )
                        HealingDivider()
                        ProfileMenuItem(
                            icon = Icons.Outlined.Feedback,
                            title = "反馈建议",
                            subtitle = "告诉我们你的想法",
                            iconColor = StitchPrimaryContainer,
                            iconBg = StitchPrimaryFixed.copy(alpha = 0.4f),
                            onClick = { navController.navigate("feedback_submit") }
                        )
                        HealingDivider()
                        ProfileMenuItem(
                            icon = Icons.Outlined.LocalHospital,
                            title = "健康资源",
                            subtitle = "专业帮助和紧急求助",
                            iconColor = StitchPrimary,
                            iconBg = StitchSecondaryContainer.copy(alpha = 0.5f),
                            onClick = { navController.navigate("resources") }
                        )
                        HealingDivider()
                        ProfileMenuItem(
                            icon = Icons.Outlined.Settings,
                            title = "设置",
                            subtitle = "提醒、密码与更多",
                            iconColor = StitchOnSurfaceVariant,
                            iconBg = StitchSurfaceContainerHighest.copy(alpha = 0.6f),
                            onClick = { navController.navigate("settings") }
                        )
                    }
                }
            }

            // 关于应用
            item {
                HealingGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    glassAlpha = 0.6f
                ) {
                    Column(modifier = Modifier.padding(HealingSpacing.CardPaddingLarge)) {
                        Text(
                            text = "关于心光",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = StitchOnSurface
                        )
                        Spacer(modifier = Modifier.height(HealingSpacing.Small))
                        Text(
                            text = "心光致力于为大学生提供安全、匿名的心理健康支持平台。通过AI技术，我们希望能够及时发现并帮助那些需要关怀的同学，成为照亮心灵的温暖之光。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = StitchOnSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(HealingSpacing.Small))
                        Text(
                            text = "版本 1.0.0",
                            style = MaterialTheme.typography.bodySmall,
                            color = StitchOnSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(HealingSpacing.XXLarge)) }
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color,
    iconBg: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(HealingSpacing.Medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(HealingSpacing.AvatarMedium)
                    .clip(HealingShapes.Small)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(HealingSpacing.Medium))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = StitchOnSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = StitchOnSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = StitchOnSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun HealingDivider() {
    Divider(
        modifier = Modifier.padding(horizontal = HealingSpacing.Medium),
        color = WarmTextLight.copy(alpha = 0.15f),
        thickness = 0.5.dp
    )
}
