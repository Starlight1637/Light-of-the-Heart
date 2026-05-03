package com.mindful.companion.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ============================================================
// 暖光疗愈系 Material3 色彩方案
// ============================================================

private val HealingDarkColorScheme = darkColorScheme(
    primary = MoonGlow,
    onPrimary = NightDeep,
    primaryContainer = NightAccent,
    onPrimaryContainer = SoftLavenderLight,

    secondary = HealingTealLight,
    onSecondary = NightDeep,
    secondaryContainer = NightSurface,
    onSecondaryContainer = HealingTealLight,

    tertiary = WarmCoralLight,
    onTertiary = NightDeep,

    background = NightDeep,
    onBackground = WarmTextOnDark,
    surface = NightSurface,
    onSurface = WarmTextOnDark,
    surfaceVariant = NightAccent,
    onSurfaceVariant = WarmTextOnDark,

    error = HealingError,
    onError = WarmTextOnDark,
)

private val HealingLightColorScheme = lightColorScheme(
    primary = StitchPrimary,
    onPrimary = Color.White,
    primaryContainer = StitchPrimaryFixed,
    onPrimaryContainer = StitchNavActiveText,

    secondary = StitchOnSurfaceVariant,
    onSecondary = Color.White,
    secondaryContainer = StitchSecondaryContainer,
    onSecondaryContainer = StitchOnSurface,

    tertiary = StitchPrimaryContainer,
    onTertiary = Color.White,
    tertiaryContainer = StitchTertiaryFixed,
    onTertiaryContainer = StitchPrimary,

    background = StitchSurface,
    onBackground = StitchOnSurface,
    surface = StitchSurface,
    onSurface = StitchOnSurface,
    surfaceVariant = StitchSurfaceContainerLow,
    onSurfaceVariant = StitchOnSurfaceVariant,

    outline = StitchOnSurfaceVariant,
    outlineVariant = StitchOutlineVariant,

    error = HealingError,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

@Composable
fun MindfulCompanionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // 禁用动态颜色，保持治愈系品牌一致性
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> HealingDarkColorScheme
        else -> HealingLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // 状态栏与背景融合，边到边沉浸式体验
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HealingTypography,
        shapes = HealingMaterialShapes,
        content = content
    )
}

