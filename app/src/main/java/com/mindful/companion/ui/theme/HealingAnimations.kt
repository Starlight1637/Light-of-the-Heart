package com.mindful.companion.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

// ============================================================
// 暖光疗愈系动画系统
// 关键原则: 聚焦高影响力时刻，避免无意义的动画堆砌
// ============================================================

object HealingAnimations {

    // --- 时长常量 ---
    object Duration {
        const val Instant = 100        // 即时反馈: 按钮状态变化
        const val Fast = 200           // 快速: 小元素淡入淡出
        const val Medium = 300         // 标准: 卡片进入、页面过渡
        const val Slow = 500           // 慢: 情绪分数动画
        const val Splash = 1500        // 启动屏: 光晕扩散
        const val PageLoad = 600       // 页面加载: 整体出现
    }

    // --- 交错动画延迟 ---
    object Stagger {
        const val Item = 50            // 列表项交错延迟 (ms)
        const val Card = 80            // 卡片交错延迟 (ms)
        const val Section = 120        // 页面区块交错延迟 (ms)
    }

    // --- Easing 曲线 ---
    object Easing {
        // 柔和出场 - 元素优雅地出现
        val Gentle: androidx.compose.animation.core.Easing =
            CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
        // 弹性出场 - 情绪分数等有活力的元素
        val Expressive: androidx.compose.animation.core.Easing =
            CubicBezierEasing(0.34f, 1.56f, 0.64f, 1.0f)
        // 标准过渡
        val Standard = FastOutSlowInEasing
        // 减速进入
        val Decelerate = LinearOutSlowInEasing
    }

    // --- Spring 弹性规格 ---
    object Spring {
        // 轻柔弹性 - 卡片、模态
        fun <T> gentle() = spring<T>(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        )
        // 中等弹性 - 按钮点击反馈
        fun <T> medium() = spring<T>(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
        )
        // 无弹性 - 平滑过渡
        fun <T> smooth() = spring<T>(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
        )
    }

    // --- 常用 AnimationSpec ---
    fun <T> fastTween() = tween<T>(
        durationMillis = Duration.Fast,
        easing = Easing.Standard
    )
    fun <T> mediumTween() = tween<T>(
        durationMillis = Duration.Medium,
        easing = Easing.Gentle
    )
    fun <T> slowTween() = tween<T>(
        durationMillis = Duration.Slow,
        easing = Easing.Gentle
    )

    // --- AI 打字机效果 ---
    object Typewriter {
        const val CharDelayMs = 30L    // 每个字符延迟 (ms)
        const val PunctuationPauseMs = 120L  // 标点后暂停
        const val InitialDelayMs = 300L      // 初始延迟 (AI思考感)
    }

    // --- 粒子系统 ---
    object Particle {
        const val Count = 12           // 粒子数量
        const val MinSizeDp = 4        // 最小尺寸
        const val MaxSizeDp = 10       // 最大尺寸
        const val AlphaMin = 0.15f     // 最小透明度
        const val AlphaMax = 0.35f     // 最大透明度
        const val SpeedMin = 0.3f      // 最慢速度
        const val SpeedMax = 0.8f      // 最快速度
    }

    // --- 启动屏动画时序 ---
    object Splash {
        const val GlowStartMs = 0
        const val GlowEndMs = 800
        const val LogoFadeStartMs = 400
        const val LogoFadeEndMs = 900
        const val NavRevealStartMs = 700
        const val NavRevealEndMs = 1400
    }
}
