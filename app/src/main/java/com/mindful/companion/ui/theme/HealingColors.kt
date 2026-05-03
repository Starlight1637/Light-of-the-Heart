package com.mindful.companion.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================
// 暖光疗愈系 (Warm Light Healing) 配色系统
// 核心隐喻: 温暖的阳光穿过薄雾，照亮内心
// ============================================================

// --- 主导色: 暖奶油色系 (约70%使用面积) ---
val WarmCream = Color(0xFFFFF8E7)       // 主背景
val SoftPeach = Color(0xFFFFE5D0)       // 次要背景
val GentleBeige = Color(0xFFF5E6D3)    // 卡片底色

// --- 强调色: 柔和色系 (约20%使用面积) ---
val HealingTeal = Color(0xFF89CFF0)    // 宁静蓝绿 - 主CTA
val HealingTealLight = Color(0xFFB8E4F8)
val HealingTealDark = Color(0xFF5AADCF)
val WarmCoral = Color(0xFFFFB5A7)      // 温暖珊瑚 - 次要强调
val WarmCoralLight = Color(0xFFFFD5CC)
val SoftLavender = Color(0xFFD4BCFA)   // 淡紫薰衣草 - AI标识
val SoftLavenderLight = Color(0xFFEADFFD)

// --- 锐利点缀色: 高饱和度小面积 (约10%) ---
val SunriseOrange = Color(0xFFFFAA5C)  // 活力点缀
val MorningPink = Color(0xFFFF85A1)    // 情感高光
val TwilightPurple = Color(0xFF9B5DE5) // 深度时刻
val GoldenHour = Color(0xFFFFCC70)     // 希望金

// --- 毛玻璃效果色 ---
val GlassSurface = Color(0x80FFFFFF)       // 白色半透明 alpha=50%
val GlassBorder = Color(0x40FFFFFF)        // 白色边框 alpha=25%
val GlassOverlay = Color(0x1AFFFFFF)       // 极浅覆盖 alpha=10%

// --- 深色模式配色 ---
val NightDeep = Color(0xFF1A1625)       // 深夜主背景
val NightSurface = Color(0xFF251E35)   // 深夜卡片
val NightAccent = Color(0xFF2D2440)    // 深夜强调
val MoonGlow = Color(0xFFE8D5FF)       // 月光紫 - 深色模式主色

// --- 文字颜色 ---
val WarmTextDark = Color(0xFF3D2C1E)   // 主文字 - 深暖棕
val WarmTextMedium = Color(0xFF6B4F3A) // 次要文字
val WarmTextLight = Color(0xFF9E8070)  // 辅助文字
val WarmTextOnDark = Color(0xFFF5EDE6) // 深色背景上的文字

// --- 风险等级 (温和化) ---
val RiskLow = Color(0xFF66BB6A)
val RiskMedium = Color(0xFFFFB74D)
val RiskHigh = Color(0xFFFF7043)
val RiskCritical = Color(0xFFEF5350)

// --- 功能性颜色 ---
val HealingSuccess = Color(0xFF7EC8A3)
val HealingWarning = Color(0xFFFFD166)
val HealingError = Color(0xFFEF5350)
val HealingInfo = HealingTeal

// ============================================================
// Stitch 设计系统颜色
// ============================================================
val StitchPrimary = Color(0xFFA43C12)
val StitchPrimaryContainer = Color(0xFFFF7F50)
val StitchPrimaryFixed = Color(0xFFFFDBCF)
val StitchPrimaryFixedDim = Color(0xFFFFB59C)
val StitchSecondaryContainer = Color(0xFFE1E1F5)
val StitchSurface = Color(0xFFFBFAEA)
val StitchSurfaceContainerLow = Color(0xFFF5F4E4)
val StitchSurfaceContainerLowest = Color(0xFFFFFFFF)
val StitchSurfaceContainerHighest = Color(0xFFE4E3D4)
val StitchOnSurface = Color(0xFF1B1C13)
val StitchOnSurfaceVariant = Color(0xFF57423B)
val StitchOutlineVariant = Color(0xFFDEC0B6)
val StitchTertiaryFixed = Color(0xFFF8DEC9)
val StitchNavActiveBg = Color(0x80FFCC99)
val StitchNavActiveText = Color(0xFF431407)
val StitchNavBarBg = Color(0xB3FAFAF2)

// --- 粒子颜色 ---
val ParticleColors = listOf(
    Color(0x4DFF85A1),  // 粉 alpha=30%
    Color(0x4D89CFF0),  // 蓝 alpha=30%
    Color(0x4DD4BCFA),  // 紫 alpha=30%
    Color(0x4DFFAA5C),  // 橙 alpha=30%
    Color(0x4D7EC8A3),  // 绿 alpha=30%
)
