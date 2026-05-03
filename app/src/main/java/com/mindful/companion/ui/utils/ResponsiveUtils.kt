package com.mindful.companion.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 响应式布局工具类
 * 根据屏幕宽度自动调整布局参数
 */
object ResponsiveUtils {
    
    /**
     * 屏幕尺寸类型
     */
    enum class ScreenSize {
        SMALL,   // < 360dp
        MEDIUM,  // 360dp - 600dp
        LARGE,   // 600dp - 840dp
        XLARGE   // > 840dp
    }
    
    /**
     * 获取当前屏幕尺寸类型
     */
    @Composable
    fun getScreenSize(): ScreenSize {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp
        
        return when {
            screenWidth < 360.dp -> ScreenSize.SMALL
            screenWidth < 600.dp -> ScreenSize.MEDIUM
            screenWidth < 840.dp -> ScreenSize.LARGE
            else -> ScreenSize.XLARGE
        }
    }
    
    /**
     * 获取响应式内边距
     */
    @Composable
    fun getResponsivePadding(): Dp {
        return when (getScreenSize()) {
            ScreenSize.SMALL -> 12.dp
            ScreenSize.MEDIUM -> 16.dp
            ScreenSize.LARGE -> 24.dp
            ScreenSize.XLARGE -> 32.dp
        }
    }
    
    /**
     * 获取响应式卡片内边距
     */
    @Composable
    fun getResponsiveCardPadding(): Dp {
        return when (getScreenSize()) {
            ScreenSize.SMALL -> 12.dp
            ScreenSize.MEDIUM -> 16.dp
            ScreenSize.LARGE -> 20.dp
            ScreenSize.XLARGE -> 24.dp
        }
    }
    
    /**
     * 获取响应式间距
     */
    @Composable
    fun getResponsiveSpacing(): Dp {
        return when (getScreenSize()) {
            ScreenSize.SMALL -> 8.dp
            ScreenSize.MEDIUM -> 12.dp
            ScreenSize.LARGE -> 16.dp
            ScreenSize.XLARGE -> 20.dp
        }
    }
    
    /**
     * 获取响应式图标大小
     */
    @Composable
    fun getResponsiveIconSize(): Dp {
        return when (getScreenSize()) {
            ScreenSize.SMALL -> 20.dp
            ScreenSize.MEDIUM -> 24.dp
            ScreenSize.LARGE -> 28.dp
            ScreenSize.XLARGE -> 32.dp
        }
    }
    
    /**
     * 获取响应式头像大小
     */
    @Composable
    fun getResponsiveAvatarSize(): Dp {
        return when (getScreenSize()) {
            ScreenSize.SMALL -> 56.dp
            ScreenSize.MEDIUM -> 64.dp
            ScreenSize.LARGE -> 72.dp
            ScreenSize.XLARGE -> 80.dp
        }
    }
    
    /**
     * 判断是否为小屏幕
     */
    @Composable
    fun isSmallScreen(): Boolean {
        return getScreenSize() == ScreenSize.SMALL
    }
    
    /**
     * 判断是否为大屏幕
     */
    @Composable
    fun isLargeScreen(): Boolean {
        val size = getScreenSize()
        return size == ScreenSize.LARGE || size == ScreenSize.XLARGE
    }
}
