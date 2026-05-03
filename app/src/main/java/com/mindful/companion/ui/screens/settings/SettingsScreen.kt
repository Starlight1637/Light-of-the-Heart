package com.mindful.companion.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.*
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
import com.mindful.companion.ui.components.HealingGlassCard
import com.mindful.companion.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showTimePicker by remember { mutableStateOf(false) }

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

            // 顶部标题行
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "返回",
                            tint = StitchOnSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "设置",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = StitchOnSurface
                    )
                }
            }

            // 通知设置
            item {
                SectionLabel("通知")
                Spacer(modifier = Modifier.height(8.dp))
                HealingGlassCard(modifier = Modifier.fillMaxWidth(), glassAlpha = 0.75f) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(HealingSpacing.Medium),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                SettingIconBox(
                                    icon = Icons.Outlined.NotificationsNone,
                                    tint = StitchPrimary,
                                    bg = StitchPrimaryFixed.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.width(HealingSpacing.Medium))
                                Column {
                                    Text(
                                        "定时打开心光",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = StitchOnSurface
                                    )
                                    Text(
                                        if (uiState.reminderEnabled) "每天 ${uiState.reminderTime} 提醒" else "已关闭",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = StitchOnSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            Switch(
                                checked = uiState.reminderEnabled,
                                onCheckedChange = { enabled ->
                                    if (enabled) showTimePicker = true
                                    else viewModel.disableReminder()
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = StitchPrimary
                                )
                            )
                        }
                        if (uiState.reminderEnabled) {
                            Divider(
                                modifier = Modifier.padding(horizontal = HealingSpacing.Medium),
                                color = StitchOutlineVariant.copy(alpha = 0.4f),
                                thickness = 0.5.dp
                            )
                            Surface(
                                onClick = { showTimePicker = true },
                                modifier = Modifier.fillMaxWidth(),
                                color = Color.Transparent
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(HealingSpacing.Medium),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "修改提醒时间",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = StitchOnSurface
                                    )
                                    Icon(
                                        Icons.Default.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = StitchOnSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 帮助与反馈
            item {
                SectionLabel("帮助与反馈")
                Spacer(modifier = Modifier.height(8.dp))
                HealingGlassCard(modifier = Modifier.fillMaxWidth(), glassAlpha = 0.75f) {
                    Column {
                        SettingRow(
                            icon = Icons.Outlined.HelpOutline,
                            iconTint = StitchPrimary,
                            iconBg = StitchPrimaryFixed.copy(alpha = 0.5f),
                            title = "使用指南",
                            onClick = { navController.navigate("resources") }
                        )
                        Divider(
                            modifier = Modifier.padding(horizontal = HealingSpacing.Medium),
                            color = StitchOutlineVariant.copy(alpha = 0.4f),
                            thickness = 0.5.dp
                        )
                        SettingRow(
                            icon = Icons.Outlined.Feedback,
                            iconTint = StitchPrimaryContainer,
                            iconBg = StitchTertiaryFixed.copy(alpha = 0.5f),
                            title = "意见反馈",
                            onClick = { navController.navigate("feedback_submit") }
                        )
                    }
                }
            }

            // 账号设置（仅登录时显示）
            if (viewModel.isLoggedIn()) {
                item {
                    SectionLabel("账号")
                    Spacer(modifier = Modifier.height(8.dp))
                    HealingGlassCard(modifier = Modifier.fillMaxWidth(), glassAlpha = 0.75f) {
                        Column {
                            SettingRow(
                                icon = Icons.Outlined.Lock,
                                iconTint = StitchPrimary,
                                iconBg = StitchSecondaryContainer.copy(alpha = 0.5f),
                                title = "修改密码",
                                onClick = { navController.navigate("change_password") }
                            )
                            Divider(
                                modifier = Modifier.padding(horizontal = HealingSpacing.Medium),
                                color = StitchOutlineVariant.copy(alpha = 0.4f),
                                thickness = 0.5.dp
                            )
                            Surface(
                                onClick = { viewModel.showLogoutDialog() },
                                modifier = Modifier.fillMaxWidth(),
                                color = Color.Transparent
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(HealingSpacing.Medium),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(HealingSpacing.AvatarMedium)
                                            .clip(HealingShapes.Small)
                                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Outlined.Logout,
                                            contentDescription = "退出登录",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(HealingSpacing.Medium))
                                    Text(
                                        "退出登录",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        Icons.Default.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(HealingSpacing.XXLarge)) }
        }
    }

    // 退出确认对话框
    if (uiState.showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissLogoutDialog() },
            containerColor = StitchSurface,
            title = { Text("退出登录", color = StitchOnSurface) },
            text = { Text("确定要退出当前账号吗？", color = StitchOnSurfaceVariant) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.logout()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                ) { Text("确定", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissLogoutDialog() }) {
                    Text("取消", color = StitchOnSurfaceVariant)
                }
            }
        )
    }

    // 时间选择器
    if (showTimePicker) {
        TimePickerDialog(
            currentTime = uiState.reminderTime,
            onTimeSelected = { hour, minute ->
                viewModel.setReminderTime(hour, minute)
                showTimePicker = false
            },
            onDismiss = {
                showTimePicker = false
                if (!uiState.reminderEnabled) viewModel.disableReminder()
            }
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = StitchOnSurfaceVariant.copy(alpha = 0.7f),
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
private fun SettingIconBox(icon: ImageVector, tint: Color, bg: Color) {
    Box(
        modifier = Modifier
            .size(HealingSpacing.AvatarMedium)
            .clip(HealingShapes.Small)
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(HealingSpacing.Medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingIconBox(icon = icon, tint = iconTint, bg = iconBg)
            Spacer(modifier = Modifier.width(HealingSpacing.Medium))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = StitchOnSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = StitchOnSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    currentTime: String,
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val parts = currentTime.split(":")
    val timePickerState = rememberTimePickerState(
        initialHour = parts[0].toInt(),
        initialMinute = parts[1].toInt()
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StitchSurface,
        title = { Text("选择提醒时间", color = StitchOnSurface) },
        text = { TimePicker(state = timePickerState) },
        confirmButton = {
            TextButton(onClick = { onTimeSelected(timePickerState.hour, timePickerState.minute) }) {
                Text("确定", color = StitchPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消", color = StitchOnSurfaceVariant) }
        }
    )
}
