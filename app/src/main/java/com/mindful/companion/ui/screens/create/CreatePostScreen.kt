package com.mindful.companion.ui.screens.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mindful.companion.ui.components.EmotionAnalysisCard
import com.mindful.companion.ui.components.SimpleAIResponseCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    navController: NavController,
    viewModel: CreatePostViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // 监听发布成功状态，成功后跳转
    LaunchedEffect(uiState.publishSuccess) {
        if (uiState.publishSuccess) {
            navController.popBackStack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("写心理日记") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 提示卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "隐私",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "这是你的私密日记本，记录你的心情和想法，AI会陪伴你、理解你。",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 文字输入框
        OutlinedTextField(
            value = uiState.content,
            onValueChange = viewModel::updateContent,
            label = { Text("写下你想说的话...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            maxLines = 10,
            placeholder = { Text("在这里，你可以安全地表达任何想法和感受...") }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 情绪分析结果
        uiState.emotionAnalysis?.let { analysis ->
            EmotionAnalysisCard(
                analysis = analysis,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // AI回复
        uiState.aiResponse?.let { response ->
            SimpleAIResponseCard(
                response = response,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // 发布按钮
        Button(
            onClick = {
                viewModel.publishPost()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.content.isNotBlank() && !uiState.isPublishing
        ) {
            if (uiState.isPublishing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(if (uiState.isPublishing) "保存中..." else "保存日记")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 分析按钮
        OutlinedButton(
            onClick = viewModel::analyzeEmotion,
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.content.isNotBlank() && !uiState.isAnalyzing
        ) {
            if (uiState.isAnalyzing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = "分析情绪"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (uiState.isAnalyzing) "分析中..." else "AI情绪分析")
        }
        
        // 错误提示
        uiState.error?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
        }
    }
}