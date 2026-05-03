package com.mindful.companion.ui.screens.report

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    navController: NavController,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadReports()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的报告") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 报告类型选择
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.reportType == ReportType.WEEK,
                    onClick = { viewModel.selectReportType(ReportType.WEEK) },
                    label = { Text("周报告") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = uiState.reportType == ReportType.MONTH,
                    onClick = { viewModel.selectReportType(ReportType.MONTH) },
                    label = { Text("月报告") },
                    modifier = Modifier.weight(1f)
                )
            }
            
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = uiState.error ?: "加载失败",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                uiState.report == null -> {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "📊",
                                style = MaterialTheme.typography.displayMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "暂无数据",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "开始记录你的心情日记吧",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                else -> {
                    val report = uiState.report!!
                    
                    // 报告标题
                    Text(
                        text = report.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = report.period,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // 情绪趋势图
                    EmotionTrendChart(
                        data = report.emotionTrend,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    )
                    
                    // 统计卡片
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            title = "记录天数",
                            value = "${report.recordDays}",
                            subtitle = "天",
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "平均情绪",
                            value = String.format("%.1f", report.averageEmotion * 100),
                            subtitle = "分",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    // 情绪分布
                    EmotionDistributionCard(report.emotionDistribution)
                    
                    // AI 分析总结
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🤖",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "AI 分析总结",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Text(
                                text = report.aiSummary,
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.5f
                            )
                        }
                    }
                    
                    // 建议
                    if (report.suggestions.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "💡 给你的建议",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                report.suggestions.forEach { suggestion ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "• ",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = suggestion,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmotionTrendChart(
    data: List<EmotionDataPoint>,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "情绪趋势",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (data.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无数据",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val padding = 40f
                    
                    // 绘制坐标轴
                    drawLine(
                        color = Color.Gray,
                        start = Offset(padding, height - padding),
                        end = Offset(width - padding, height - padding),
                        strokeWidth = 2f
                    )
                    drawLine(
                        color = Color.Gray,
                        start = Offset(padding, padding),
                        end = Offset(padding, height - padding),
                        strokeWidth = 2f
                    )
                    
                    // 绘制折线
                    if (data.size > 1) {
                        val path = Path()
                        val stepX = (width - 2 * padding) / (data.size - 1)
                        val maxY = height - 2 * padding
                        
                        data.forEachIndexed { index, point ->
                            val x = padding + index * stepX
                            val y = height - padding - (point.value * maxY)
                            
                            if (index == 0) {
                                path.moveTo(x, y)
                            } else {
                                path.lineTo(x, y)
                            }
                            
                            // 绘制数据点
                            drawCircle(
                                color = Color(0xFF2196F3),
                                radius = 6f,
                                center = Offset(x, y)
                            )
                        }
                        
                        drawPath(
                            path = path,
                            color = Color(0xFF2196F3),
                            style = Stroke(width = 3f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EmotionDistributionCard(distribution: Map<String, Float>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "情绪分布",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            distribution.forEach { (emotion, percentage) ->
                EmotionBar(
                    emotion = emotion,
                    percentage = percentage
                )
            }
        }
    }
}

@Composable
fun EmotionBar(
    emotion: String,
    percentage: Float
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = emotion,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${(percentage * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = percentage,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = getEmotionColor(emotion)
        )
    }
}

fun getEmotionColor(emotion: String): Color {
    return when (emotion) {
        "快乐" -> Color(0xFF4CAF50)
        "平静" -> Color(0xFF2196F3)
        "焦虑" -> Color(0xFFFF9800)
        "悲伤" -> Color(0xFF9C27B0)
        "愤怒" -> Color(0xFFF44336)
        else -> Color.Gray
    }
}
