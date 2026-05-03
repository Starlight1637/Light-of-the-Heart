package com.mindful.companion.ui.screens.resources

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

// ----------------------------------------------------------------
// 数据模型
// ----------------------------------------------------------------

enum class ResourceCategory { HOTLINE, SCIENCE, SELF_TEST, CBT, EMOTION, MINDFULNESS }

data class ResourceItem(
    val title: String,
    val description: String,
    val category: ResourceCategory,
    val action: String,       // URL 或 "tel:XXXXX" 格式
    val actionLabel: String   // 按钮文字
)

// ----------------------------------------------------------------
// 真实资源数据（来源：国家卫健委、教育部、北大六院等权威机构）
// ----------------------------------------------------------------

private val HOTLINES = listOf(
    ResourceItem(
        title = "全国统一心理援助热线",
        description = "国家卫健委指定，24小时专业心理援助",
        category = ResourceCategory.HOTLINE,
        action = "tel:12356",
        actionLabel = "拨打 12356"
    ),
    ResourceItem(
        title = "北京心理危机干预中心",
        description = "北京回龙观医院，全国权威危机干预机构，24小时",
        category = ResourceCategory.HOTLINE,
        action = "tel:01082951332",
        actionLabel = "拨打 010-82951332"
    ),
    ResourceItem(
        title = "全国心理援助热线",
        description = "全国各地心理援助热线汇总（教育部整理）",
        category = ResourceCategory.HOTLINE,
        action = "https://dxs.moe.gov.cn/zx/a/xljk_xlyzrx/230912/1859431.shtml",
        actionLabel = "查看热线名单"
    )
)

private val RESOURCES = listOf(
    // 权威科普
    ResourceItem(
        title = "国家心理健康防治中心",
        description = "国家卫健委直属，提供权威心理健康政策与科普",
        category = ResourceCategory.SCIENCE,
        action = "https://ncmhc.org.cn/",
        actionLabel = "前往查看"
    ),
    ResourceItem(
        title = "中国大学生在线·心理健康",
        description = "教育部官方平台，专为大学生设计的心理健康资源",
        category = ResourceCategory.SCIENCE,
        action = "https://dxs.moe.gov.cn/zx/xljk/",
        actionLabel = "前往查看"
    ),
    ResourceItem(
        title = "北京大学第六医院",
        description = "国家精神卫生三甲专科医院，WHO合作中心",
        category = ResourceCategory.SCIENCE,
        action = "https://www.pkuh6.cn/",
        actionLabel = "前往查看"
    ),
    // 自测工具
    ResourceItem(
        title = "PHQ-9 抑郁筛查量表",
        description = "临床标准抑郁程度自评工具，附专业评分解读",
        category = ResourceCategory.SELF_TEST,
        action = "https://m.medsci.cn/scale/show.do?id=291e1050f3",
        actionLabel = "开始自测"
    ),
    ResourceItem(
        title = "SAS 焦虑自评量表",
        description = "Zung 标准化焦虑自评量表，了解自身焦虑程度",
        category = ResourceCategory.SELF_TEST,
        action = "https://m.medsci.cn/scale/show.do?id=87b111489",
        actionLabel = "开始自测"
    ),
    // CBT 自助
    ResourceItem(
        title = "认知行为疗法（CBT）科普",
        description = "英国皇家精神科医学院中文版，权威CBT原理与方法",
        category = ResourceCategory.CBT,
        action = "https://www.rcpsych.ac.uk/mental-health/translations/chinese",
        actionLabel = "阅读科普"
    ),
    // 情绪管理
    ResourceItem(
        title = "大学生情绪管理微课",
        description = "教育部官方微课，11种应对情绪困扰的实用方法",
        category = ResourceCategory.EMOTION,
        action = "https://dxs.moe.gov.cn/zx/a/xl_xlwk/211209/1740650.shtml",
        actionLabel = "观看微课"
    ),
    ResourceItem(
        title = "5·25 大学生心理健康专题",
        description = "教育部2024年心理健康日专题，覆盖情绪·压力·人际",
        category = ResourceCategory.EMOTION,
        action = "https://dxs.moe.gov.cn/zx/xl/525dxsxljkzt2024/",
        actionLabel = "前往查看"
    ),
    // 正念冥想
    ResourceItem(
        title = "正念减压训练（MBSR）",
        description = "华东师范大学心理中心，正念理论与实践专题资料",
        category = ResourceCategory.MINDFULNESS,
        action = "https://xlzx.ecnu.edu.cn/4f/2a/c6834a413482/page.htm",
        actionLabel = "学习正念"
    ),
    ResourceItem(
        title = "睡眠健康核心信息",
        description = "国家卫健委2025年发布，8条科学睡眠指导",
        category = ResourceCategory.MINDFULNESS,
        action = "https://www.nhc.gov.cn/guihuaxxs/c100133/202503/70d5836afe804a858b899ee951a24a13.shtml",
        actionLabel = "阅读指南"
    )
)

// ----------------------------------------------------------------
// UI
// ----------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourcesScreen(navController: NavController) {
    val context = LocalContext.current

    fun openAction(action: String) {
        val uri = Uri.parse(action)
        val intent = if (action.startsWith("tel:")) {
            Intent(Intent.ACTION_DIAL, uri)
        } else {
            Intent(Intent.ACTION_VIEW, uri)
        }
        context.startActivity(intent)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("心理健康资源") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // 紧急求助卡片
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Emergency,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "紧急求助",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "如果你正在经历严重的心理危机，请立即拨打：",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { openAction("tel:12356") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("12356（全国）")
                            }
                            OutlinedButton(
                                onClick = { openAction("tel:01082951332") }
                            ) {
                                Text("010-82951332")
                            }
                        }
                    }
                }
            }

            // 热线
            item { SectionHeader("危机热线", Icons.Default.Phone) }
            items(HOTLINES) { res ->
                ResourceCard(res, onAction = { openAction(res.action) })
            }

            // 其他资源
            item { SectionHeader("科普与自助资源", Icons.Default.MenuBook) }
            items(RESOURCES) { res ->
                ResourceCard(res, onAction = { openAction(res.action) })
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ResourceCard(item: ResourceItem, onAction: () -> Unit) {
    val categoryIcon = when (item.category) {
        ResourceCategory.HOTLINE -> Icons.Default.Phone
        ResourceCategory.SCIENCE -> Icons.Default.Science
        ResourceCategory.SELF_TEST -> Icons.Default.Assignment
        ResourceCategory.CBT -> Icons.Default.Psychology
        ResourceCategory.EMOTION -> Icons.Default.Favorite
        ResourceCategory.MINDFULNESS -> Icons.Default.SelfImprovement
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onAction)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                categoryIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(8.dp))
            TextButton(onClick = onAction) {
                Text(item.actionLabel, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
