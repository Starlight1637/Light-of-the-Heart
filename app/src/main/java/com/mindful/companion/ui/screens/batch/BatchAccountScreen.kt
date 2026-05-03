package com.mindful.companion.ui.screens.batch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mindful.companion.ui.theme.*

/**
 * Batch account creation screen for administrators
 * Requirements: 6.1, 6.2, 6.3, 6.5
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchAccountScreen(
    navController: NavController,
    viewModel: BatchAccountViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Show error snackbar
    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            // Error will be shown in the UI
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("批量创建账号") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (uiState.result != null) {
            // Show result screen
            ResultScreen(
                result = uiState.result!!,
                onCreateAnother = {
                    viewModel.clearResult()
                    viewModel.resetForm()
                },
                onBack = { navController.navigateUp() }
            )
        } else {
            // Show form screen
            FormScreen(
                modifier = Modifier.padding(paddingValues),
                uiState = uiState,
                onSchoolChange = viewModel::updateSchool,
                onAccountStartChange = viewModel::updateAccountStart,
                onAccountEndChange = viewModel::updateAccountEnd,
                onRoleChange = viewModel::updateRole,
                onCreateClick = viewModel::createBatchAccounts,
                onErrorDismiss = viewModel::clearError
            )
        }
    }
}

@Composable
private fun FormScreen(
    modifier: Modifier = Modifier,
    uiState: BatchAccountUiState,
    onSchoolChange: (String) -> Unit,
    onAccountStartChange: (String) -> Unit,
    onAccountEndChange: (String) -> Unit,
    onRoleChange: (String) -> Unit,
    onCreateClick: () -> Unit,
    onErrorDismiss: () -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "批量创建学生账号",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            Text(
                text = "请填写以下信息以批量创建账号。账号的默认密码将与账号相同。",
                style = MaterialTheme.typography.bodyMedium,
                color = SubtleText
            )
        }
        
        // School selection
        item {
            SchoolSelectionCard(
                selectedSchool = uiState.school,
                onSchoolChange = onSchoolChange
            )
        }
        
        // Account range input
        item {
            AccountRangeCard(
                accountStart = uiState.accountStart,
                accountEnd = uiState.accountEnd,
                onAccountStartChange = onAccountStartChange,
                onAccountEndChange = onAccountEndChange
            )
        }
        
        // Role selection
        item {
            RoleSelectionCard(
                selectedRole = uiState.role,
                onRoleChange = onRoleChange
            )
        }
        
        // Error message
        if (uiState.error != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = HighRisk.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "错误",
                            tint = HighRisk,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = uiState.error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = HighRisk,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onErrorDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "关闭",
                                tint = HighRisk
                            )
                        }
                    }
                }
            }
        }
        
        // Create button
        item {
            Button(
                onClick = onCreateClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("创建中...")
                } else {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("创建账号", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SchoolSelectionCard(
    selectedSchool: String,
    onSchoolChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val schools = listOf("心光大学")
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "学校",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedSchool,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("选择学校") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        focusedLabelColor = Primary
                    )
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    schools.forEach { school ->
                        DropdownMenuItem(
                            text = { Text(school) },
                            onClick = {
                                onSchoolChange(school)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountRangeCard(
    accountStart: String,
    accountEnd: String,
    onAccountStartChange: (String) -> Unit,
    onAccountEndChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "账号范围",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = accountStart,
                    onValueChange = onAccountStartChange,
                    label = { Text("起始账号") },
                    placeholder = { Text("例如: 1001") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        focusedLabelColor = Primary
                    ),
                    singleLine = true
                )
                
                Text(
                    text = "至",
                    style = MaterialTheme.typography.bodyLarge,
                    color = SubtleText
                )
                
                OutlinedTextField(
                    value = accountEnd,
                    onValueChange = onAccountEndChange,
                    label = { Text("结束账号") },
                    placeholder = { Text("例如: 1050") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        focusedLabelColor = Primary
                    ),
                    singleLine = true
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Show account count
            val start = accountStart.toIntOrNull()
            val end = accountEnd.toIntOrNull()
            if (start != null && end != null && start <= end) {
                val count = end - start + 1
                Text(
                    text = "将创建 $count 个账号",
                    style = MaterialTheme.typography.bodySmall,
                    color = Primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun RoleSelectionCard(
    selectedRole: String,
    onRoleChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "账号角色",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RoleOption(
                    title = "普通用户",
                    description = "学生账号",
                    icon = Icons.Default.Person,
                    isSelected = selectedRole == "user",
                    onClick = { onRoleChange("user") },
                    modifier = Modifier.weight(1f)
                )
                
                RoleOption(
                    title = "管理员",
                    description = "管理权限",
                    icon = Icons.Default.AdminPanelSettings,
                    isSelected = selectedRole == "admin",
                    onClick = { onRoleChange("admin") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoleOption(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Primary.copy(alpha = 0.1f) else Color.Transparent
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 2.dp,
            color = if (isSelected) Primary else Color.Gray.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) Primary else SubtleText,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Primary else DarkText
            )
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = SubtleText
            )
        }
    }
}

@Composable
private fun ResultScreen(
    result: com.mindful.companion.data.model.BatchAccountResponse,
    onCreateAnother: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Success icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = if (result.failedCount == 0) LowRisk.copy(alpha = 0.2f) else MediumRisk.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(40.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (result.failedCount == 0) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (result.failedCount == 0) LowRisk else MediumRisk,
                modifier = Modifier.size(48.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = if (result.failedCount == 0) "创建成功！" else "部分创建成功",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = result.message,
            style = MaterialTheme.typography.bodyMedium,
            color = SubtleText
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Statistics card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatRow(
                    label = "请求创建",
                    value = "${result.totalRequested} 个",
                    color = DarkText
                )
                
                StatRow(
                    label = "成功创建",
                    value = "${result.successCount} 个",
                    color = LowRisk
                )
                
                if (result.failedCount > 0) {
                    StatRow(
                        label = "创建失败",
                        value = "${result.failedCount} 个",
                        color = HighRisk
                    )
                }
                
                if (result.skippedAccounts.isNotEmpty()) {
                    Divider()
                    
                    Text(
                        text = "跳过的账号（已存在）",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = result.skippedAccounts.joinToString(", "),
                        style = MaterialTheme.typography.bodySmall,
                        color = SubtleText
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Action buttons
        Button(
            onClick = onCreateAnother,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Primary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("继续创建", style = MaterialTheme.typography.titleMedium)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("返回", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = DarkText
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
