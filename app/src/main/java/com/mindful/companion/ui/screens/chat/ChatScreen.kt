package com.mindful.companion.ui.screens.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mindful.companion.ui.components.AITypewriterText
import com.mindful.companion.ui.components.HealingGlassCard
import com.mindful.companion.ui.components.HealingOutlineButton
import com.mindful.companion.ui.components.HealingPrimaryButton
import com.mindful.companion.ui.components.HealingTextField
import com.mindful.companion.ui.components.ParticleBackground
import com.mindful.companion.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    LaunchedEffect(Unit) { viewModel.loadChatSessions() }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerContainerColor = StitchSurface
            ) {
                ChatHistoryDrawer(
                    sessions = uiState.chatSessions,
                    currentSessionId = uiState.currentSessionId,
                    onSessionClick = { sessionId ->
                        viewModel.loadSession(sessionId)
                        scope.launch { drawerState.close() }
                    },
                    onNewChat = {
                        viewModel.startNewChat()
                        scope.launch { drawerState.close() }
                    },
                    onDeleteSession = { viewModel.deleteSession(it) }
                )
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(StitchSurface, StitchSurfaceContainerLow, StitchSurface)
                    )
                )
        ) {
            ParticleBackground(particleCount = 6)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = HealingSpacing.ScreenPadding)
            ) {
                Spacer(modifier = Modifier.height(HealingSpacing.Large))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "倾诉",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = StitchOnSurface
                    )
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "历史对话",
                            tint = StitchOnSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(HealingSpacing.Medium))

                if (uiState.messages.isEmpty()) {
                    HealingGlassCard(modifier = Modifier.fillMaxWidth(), glassAlpha = 0.75f) {
                        Column(modifier = Modifier.padding(HealingSpacing.CardPaddingLarge)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(StitchSecondaryContainer.copy(alpha = 0.6f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Psychology,
                                        contentDescription = null,
                                        tint = StitchPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(HealingSpacing.Small))
                                Text(
                                    text = "你好，我是心光",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = StitchOnSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(HealingSpacing.Small))
                            Text(
                                text = "我会倾听你的烦恼，陪你聊天，给你温暖的建议。请放心，我们的对话是完全保密的。",
                                style = MaterialTheme.typography.bodyMedium,
                                color = StitchOnSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(HealingSpacing.Medium))
                }

                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(HealingSpacing.ItemSpacing)
                ) {
                    items(uiState.messages) { message ->
                        ChatMessageItem(message = message)
                    }

                    if (uiState.isAiTyping) {
                        item {
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                                exit = fadeOut()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(HealingShapes.BubbleLeft)
                                            .background(StitchSecondaryContainer.copy(alpha = 0.5f))
                                            .padding(
                                                horizontal = HealingSpacing.Medium,
                                                vertical = HealingSpacing.Small
                                            )
                                    ) {
                                        Text(
                                            text = "心光正在思考 ···",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = StitchPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(HealingSpacing.Medium)) }
                }

                if (uiState.showEndButton && !uiState.isAiTyping) {
                    HealingOutlineButton(
                        text = if (uiState.isGeneratingReport) "生成心情报告中..." else "就聊到这里吧",
                        onClick = { viewModel.endChatSession() },
                        enabled = !uiState.isGeneratingReport,
                        modifier = Modifier.padding(bottom = HealingSpacing.Small)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = HealingSpacing.Medium),
                    verticalAlignment = Alignment.Bottom
                ) {
                    HealingTextField(
                        value = uiState.inputText,
                        onValueChange = viewModel::updateInputText,
                        placeholder = "说说你的想法...",
                        maxLines = 4,
                        minLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(HealingSpacing.XSmall))
                    val canSend = uiState.inputText.isNotBlank() && !uiState.isAiTyping
                    FilledIconButton(
                        onClick = viewModel::sendMessage,
                        enabled = canSend,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (canSend) StitchPrimaryContainer else StitchSurfaceContainerHighest
                        ),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "发送",
                            tint = if (canSend) Color.White else StitchOnSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            if (uiState.moodReport != null) {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissMoodReport() },
                    containerColor = StitchSurface,
                    title = {
                        Text(
                            "心情报告",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = StitchOnSurface
                        )
                    },
                    text = {
                        Column {
                            Text(uiState.moodReport!!, style = MaterialTheme.typography.bodyMedium, color = StitchOnSurfaceVariant)
                            Spacer(modifier = Modifier.height(HealingSpacing.Medium))
                            HealingOutlineButton(
                                text = "发送给管理员",
                                onClick = { viewModel.showSendToAdminDialog() },
                                icon = Icons.Default.Send
                            )
                        }
                    },
                    confirmButton = {
                        HealingPrimaryButton(
                            text = "开始新的聊天",
                            onClick = { viewModel.dismissMoodReport() },
                            modifier = Modifier.width(160.dp)
                        )
                    }
                )
            }

            if (uiState.showSendToAdminDialog) {
                AlertDialog(
                    onDismissRequest = { if (!uiState.isSendingToAdmin) viewModel.dismissSendToAdminDialog() },
                    containerColor = StitchSurface,
                    title = {
                        Text(
                            when {
                                uiState.sendToAdminSuccess == true -> "发送成功 ✓"
                                uiState.sendToAdminSuccess == false -> "发送失败"
                                else -> "发送给管理员"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = StitchOnSurface
                        )
                    },
                    text = {
                        when {
                            uiState.isSendingToAdmin -> Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = StitchPrimaryContainer)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("正在发送...", color = StitchOnSurfaceVariant)
                            }
                            uiState.sendToAdminSuccess == true ->
                                Text("你的心情报告已成功发送给管理员。管理员会查看并在需要时提供帮助。", color = StitchOnSurfaceVariant)
                            uiState.sendToAdminSuccess == false ->
                                Text("发送失败，请稍后重试。", color = RiskHigh)
                            else ->
                                Text("确定要将这份心情报告发送给管理员吗？管理员可以查看你的报告内容，以便在需要时提供帮助。", color = StitchOnSurfaceVariant)
                        }
                    },
                    confirmButton = {
                        if (uiState.sendToAdminSuccess != null) {
                            HealingPrimaryButton(
                                text = "确定",
                                onClick = { viewModel.dismissSendToAdminDialog() },
                                modifier = Modifier.width(100.dp)
                            )
                        } else if (!uiState.isSendingToAdmin) {
                            HealingPrimaryButton(
                                text = "发送",
                                onClick = { viewModel.sendReportToAdmin() },
                                modifier = Modifier.width(100.dp)
                            )
                        }
                    },
                    dismissButton = {
                        if (uiState.sendToAdminSuccess == null && !uiState.isSendingToAdmin) {
                            TextButton(onClick = { viewModel.dismissSendToAdminDialog() }) {
                                Text("取消", color = StitchOnSurfaceVariant)
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
    val isUser = message.isFromUser

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(StitchSecondaryContainer.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = "AI",
                    tint = StitchPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(HealingSpacing.XSmall))
        }

        Box(
            modifier = Modifier
                .widthIn(max = 270.dp)
                .clip(if (isUser) HealingShapes.BubbleRight else HealingShapes.BubbleLeft)
                .background(
                    if (isUser) StitchPrimaryContainer.copy(alpha = 0.85f)
                    else StitchSecondaryContainer.copy(alpha = 0.5f)
                )
                .padding(horizontal = HealingSpacing.Medium, vertical = HealingSpacing.Small)
        ) {
            Column {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) StitchNavActiveText else StitchOnSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatTime(message.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isUser) StitchNavActiveText.copy(alpha = 0.65f) else StitchOnSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(HealingSpacing.XSmall))
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(StitchPrimaryFixed),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "用户",
                    tint = StitchPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

fun formatTime(date: Date): String {
    val diff = Date().time - date.time
    return when {
        diff < 60_000 -> "刚刚"
        diff < 3_600_000 -> "${diff / 60_000}分钟前"
        else -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
    }
}

@Composable
fun ChatHistoryDrawer(
    sessions: List<ChatSessionItem>,
    currentSessionId: String?,
    onSessionClick: (String) -> Unit,
    onNewChat: () -> Unit,
    onDeleteSession: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(HealingSpacing.Medium)) {
        Text(
            text = "历史对话",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = StitchOnSurface
        )

        Spacer(modifier = Modifier.height(HealingSpacing.Medium))

        HealingPrimaryButton(text = "新对话", onClick = onNewChat, icon = Icons.Default.Add)

        Spacer(modifier = Modifier.height(HealingSpacing.Medium))

        if (sessions.isEmpty()) {
            Text(
                text = "还没有历史对话",
                style = MaterialTheme.typography.bodyMedium,
                color = StitchOnSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(vertical = HealingSpacing.XXLarge)
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(HealingSpacing.XSmall)) {
                items(sessions) { session ->
                    ChatSessionCard(
                        session = session,
                        isSelected = session.id == currentSessionId,
                        onClick = { onSessionClick(session.id) },
                        onDelete = { onDeleteSession(session.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatSessionCard(
    session: ChatSessionItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDelete by remember { mutableStateOf(false) }

    HealingGlassCard(onClick = onClick, glassAlpha = if (isSelected) 0.85f else 0.65f) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(HealingSpacing.Small),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = StitchOnSurface,
                    maxLines = 1
                )
                Text(
                    text = session.preview,
                    style = MaterialTheme.typography.bodySmall,
                    color = StitchOnSurfaceVariant.copy(alpha = 0.6f),
                    maxLines = 2
                )
            }
            IconButton(onClick = { showDelete = true }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "删除", modifier = Modifier.size(16.dp), tint = StitchOnSurfaceVariant.copy(alpha = 0.5f))
            }
        }
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            containerColor = StitchSurface,
            title = { Text("删除对话", color = StitchOnSurface) },
            text = { Text("确定要删除这段对话吗？", color = StitchOnSurfaceVariant) },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDelete = false }) {
                    Text("删除", color = RiskHigh)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) {
                    Text("取消", color = StitchOnSurfaceVariant)
                }
            }
        )
    }
}
