package com.mindful.companion.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mindful.companion.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    // 登录成功后跳转
    LaunchedEffect(uiState.isLoggedIn, uiState.isAdmin) {
        if (uiState.isLoggedIn) {
            navController.navigate("chat") {
                popUpTo("login") { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        StitchSurface,
                        StitchPrimaryFixed.copy(alpha = 0.35f),
                        StitchSurface
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(72.dp))

            // Logo 区域
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(StitchPrimaryFixed, StitchTertiaryFixed)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "心光",
                    modifier = Modifier.size(44.dp),
                    tint = StitchPrimary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "心光",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = StitchOnSurface
            )
            Text(
                text = "大学生心理健康陪伴",
                style = MaterialTheme.typography.bodyMedium,
                color = StitchOnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(40.dp))

            // 表单卡片
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.75f),
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 学校选择
                    ExposedDropdownMenuBox(
                        expanded = uiState.schoolDropdownExpanded,
                        onExpandedChange = { viewModel.toggleSchoolDropdown() }
                    ) {
                        OutlinedTextField(
                            value = uiState.selectedSchool,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("学校") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = uiState.schoolDropdownExpanded
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = uiState.schoolDropdownExpanded,
                            onDismissRequest = { viewModel.toggleSchoolDropdown() }
                        ) {
                            uiState.schools.forEach { school ->
                                DropdownMenuItem(
                                    text = { Text(school) },
                                    onClick = {
                                        viewModel.selectSchool(school)
                                        viewModel.toggleSchoolDropdown()
                                    }
                                )
                            }
                        }
                    }

                    // 账号输入
                    OutlinedTextField(
                        value = uiState.account,
                        onValueChange = { viewModel.updateAccount(it) },
                        label = { Text("账号") },
                        placeholder = { Text("请输入账号（如：0001）") },
                        leadingIcon = { Icon(Icons.Default.Person, "账号") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // 密码输入
                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = { viewModel.updatePassword(it) },
                        label = { Text("密码") },
                        placeholder = { Text("请输入密码") },
                        leadingIcon = { Icon(Icons.Default.Lock, "密码") },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "隐藏密码" else "显示密码"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // 错误提示
                    if (uiState.errorMessage != null) {
                        Text(
                            text = uiState.errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // 登录按钮
                    Button(
                        onClick = { viewModel.login() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        enabled = !uiState.isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = StitchPrimary)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Text("登录", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 先看看
            TextButton(
                onClick = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            ) {
                Text(
                    "先看看  →",
                    style = MaterialTheme.typography.bodyMedium,
                    color = StitchOnSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 提示信息
            Text(
                text = "初始密码与账号相同 · 账号范围 0001–2025",
                style = MaterialTheme.typography.bodySmall,
                color = StitchOnSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
