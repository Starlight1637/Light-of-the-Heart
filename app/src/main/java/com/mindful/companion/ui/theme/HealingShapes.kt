package com.mindful.companion.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ============================================================
// 暖光疗愈系圆角系统
// 大圆角传递柔软、安全、治愈的感受
// ============================================================

object HealingShapes {
    val ExtraSmall = RoundedCornerShape(8.dp)   // 微元素: 标签、角标
    val Small = RoundedCornerShape(12.dp)        // 小元素: 输入框、小按钮
    val Medium = RoundedCornerShape(16.dp)       // 标准按钮、小卡片
    val Large = RoundedCornerShape(20.dp)        // 次要卡片
    val ExtraLarge = RoundedCornerShape(24.dp)  // 主卡片
    val XXLarge = RoundedCornerShape(32.dp)     // 底部弹窗、大模态
    val Full = RoundedCornerShape(50)           // 全圆角: 头像、Chip、FAB

    // 不对称圆角 - 用于特定场景增加趣味性
    val BubbleLeft = RoundedCornerShape(        // AI消息气泡
        topStart = 4.dp,
        topEnd = 20.dp,
        bottomEnd = 20.dp,
        bottomStart = 20.dp
    )
    val BubbleRight = RoundedCornerShape(       // 用户消息气泡
        topStart = 20.dp,
        topEnd = 4.dp,
        bottomEnd = 20.dp,
        bottomStart = 20.dp
    )
    val CardTop = RoundedCornerShape(           // 仅顶部圆角卡片
        topStart = 24.dp,
        topEnd = 24.dp,
        bottomEnd = 0.dp,
        bottomStart = 0.dp
    )
    val CardBottom = RoundedCornerShape(        // 仅底部圆角卡片
        topStart = 0.dp,
        topEnd = 0.dp,
        bottomEnd = 24.dp,
        bottomStart = 24.dp
    )
}

// Material3 Shapes 映射（供 MaterialTheme 使用）
val HealingMaterialShapes = Shapes(
    extraSmall = HealingShapes.ExtraSmall,
    small = HealingShapes.Small,
    medium = HealingShapes.Medium,
    large = HealingShapes.Large,
    extraLarge = HealingShapes.ExtraLarge
)
