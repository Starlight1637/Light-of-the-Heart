package com.mindful.companion.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.mindful.companion.ui.theme.HealingAnimations
import kotlinx.coroutines.delay

// ============================================================
// AI 打字机效果文字组件
// 逐字符显示，营造AI思考与表达的真实感
// ============================================================

/**
 * @param text 要显示的完整文本
 * @param onFinished 打字完成回调
 * @param isStreaming 是否处于流式输出状态（true时文字持续追加）
 */
@Composable
fun AITypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    onFinished: () -> Unit = {}
) {
    var displayedText by remember(text) { mutableStateOf("") }
    var isComplete by remember(text) { mutableStateOf(false) }

    LaunchedEffect(text) {
        displayedText = ""
        isComplete = false

        // 初始延迟，模拟AI思考
        delay(HealingAnimations.Typewriter.InitialDelayMs)

        for (i in text.indices) {
            displayedText = text.substring(0, i + 1)

            // 标点符号后停顿更长
            val char = text[i]
            val delayMs = when (char) {
                '。', '！', '？', '…', '\n' -> HealingAnimations.Typewriter.PunctuationPauseMs
                '，', '、', '；', '：' -> HealingAnimations.Typewriter.PunctuationPauseMs / 2
                else -> HealingAnimations.Typewriter.CharDelayMs
            }
            delay(delayMs)
        }

        isComplete = true
        onFinished()
    }

    Text(
        text = displayedText,
        modifier = modifier,
        style = style,
        color = color
    )
}

/**
 * 简单版：已完成的文字直接展示，不走打字机（用于历史消息）
 */
@Composable
fun StaticAIText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified
) {
    Text(text = text, modifier = modifier, style = style, color = color)
}
