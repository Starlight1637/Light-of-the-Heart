package com.mindful.companion.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ============================================================
// 暖光疗愈系字体系统
//
// 当前状态: 使用系统字体，可随时替换为自定义字体
//
// 升级为自定义字体的步骤:
// 1. 下载 霞鹜文楷 (https://github.com/lxgw/LxgwWenKai)
//    → 放置 lxgw_wenkai_regular.ttf / lxgw_wenkai_bold.ttf
//      到 app/src/main/res/font/
// 2. 下载 Caveat (https://fonts.google.com/specimen/Caveat)
//    → 放置 caveat_regular.ttf / caveat_bold.ttf
//      到 app/src/main/res/font/
// 3. 取消注释下方字体声明并替换 FontFamily.SansSerif
// ============================================================

// --- 字体家族声明 (有字体文件时取消注释) ---
// val LxgwWenKai = FontFamily(
//     Font(R.font.lxgw_wenkai_regular, FontWeight.Normal),
//     Font(R.font.lxgw_wenkai_bold, FontWeight.Bold)
// )
// val CaveatFont = FontFamily(
//     Font(R.font.caveat_regular, FontWeight.Normal),
//     Font(R.font.caveat_bold, FontWeight.Bold)
// )

// 当前使用系统字体 (待自定义字体下载后替换)
val HealingBodyFont = FontFamily.SansSerif    // 替换为 LxgwWenKai
val HealingDisplayFont = FontFamily.SansSerif  // 替换为 源柔黑或系统圆润字体
val HealingHandwritingFont = FontFamily.Cursive // 替换为 CaveatFont

// Material3 Typography
val HealingTypography = Typography(
    // 大标题 - 页面主标题
    displayLarge = TextStyle(
        fontFamily = HealingDisplayFont,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = HealingDisplayFont,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.25).sp
    ),
    displaySmall = TextStyle(
        fontFamily = HealingDisplayFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    // 标题
    headlineLarge = TextStyle(
        fontFamily = HealingDisplayFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = HealingDisplayFont,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = HealingDisplayFont,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    ),
    // 正文
    bodyLarge = TextStyle(
        fontFamily = HealingBodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 26.sp,   // 宽行距增加阅读舒适感
        letterSpacing = 0.3.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = HealingBodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.2.sp
    ),
    bodySmall = TextStyle(
        fontFamily = HealingBodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.2.sp
    ),
    // 标签
    labelLarge = TextStyle(
        fontFamily = HealingDisplayFont,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = HealingDisplayFont,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = HealingDisplayFont,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    ),
    // 标题栏
    titleLarge = TextStyle(
        fontFamily = HealingDisplayFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = HealingDisplayFont,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = HealingDisplayFont,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
)

// 手写标注样式（供 AI 回复旁温暖标注使用）
val HandwritingAnnotationStyle = TextStyle(
    fontFamily = HealingHandwritingFont,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.sp
)
