package com.mindful.companion.ui.theme

import androidx.compose.ui.unit.dp

// ============================================================
// 暖光疗愈系间距系统
// 充足的呼吸感让界面温柔而不拥挤
// ============================================================

object HealingSpacing {
    // 基础间距单元 = 4dp
    val XXSmall = 4.dp     // 紧密元素间距
    val XSmall = 8.dp      // 小元素内边距
    val Small = 12.dp      // 紧凑内边距
    val Medium = 16.dp     // 主要间距（最常用）
    val Large = 20.dp      // 段落间距
    val XLarge = 24.dp     // 区块间距
    val XXLarge = 32.dp    // 页面级间距
    val XXXLarge = 48.dp   // 特殊大间距

    // 语义化间距
    val CardPadding = Medium            // 卡片内边距
    val CardPaddingLarge = XLarge       // 大卡片内边距
    val ScreenPadding = Medium          // 屏幕水平边距
    val SectionSpacing = XXLarge        // 页面内分区间距
    val ItemSpacing = Small             // 列表项目间距
    val IconTextSpacing = XSmall        // 图标与文字间距

    // 底部导航相关
    val BottomNavHeight = 80.dp
    val BottomNavPaddingBottom = 16.dp

    // 按钮
    val ButtonVerticalPadding = 14.dp
    val ButtonHorizontalPadding = 24.dp
    val ButtonHeight = 52.dp           // 主要按钮高度

    // 输入框
    val TextFieldHeight = 56.dp
    val TextFieldPadding = Medium

    // 头像
    val AvatarSmall = 32.dp
    val AvatarMedium = 44.dp
    val AvatarLarge = 64.dp

    // 图标
    val IconSmall = 16.dp
    val IconMedium = 24.dp
    val IconLarge = 32.dp
    val IconXLarge = 48.dp
}
