package com.mindful.companion.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.mindful.companion.ui.theme.HealingAnimations
import com.mindful.companion.ui.theme.HealingShapes
import com.mindful.companion.ui.theme.HealingSpacing
import com.mindful.companion.ui.theme.WarmTextDark
import com.mindful.companion.ui.theme.WarmTextLight

// ============================================================
// 治愈系毛玻璃输入框
// ============================================================

@Composable
fun HealingTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    label: String? = null,
    maxLines: Int = 1,
    minLines: Int = 1,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    var isFocused by remember { mutableStateOf(false) }

    val borderColor by animateColorAsState(
        targetValue = if (isFocused)
            MaterialTheme.colorScheme.primary
        else
            Color.White.copy(alpha = 0.4f),
        animationSpec = HealingAnimations.fastTween(),
        label = "border_color"
    )

    val bgAlpha by animateColorAsState(
        targetValue = if (isFocused)
            MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
        else
            MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
        animationSpec = HealingAnimations.fastTween(),
        label = "bg_alpha"
    )

    Column(modifier = modifier) {
        label?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            maxLines = maxLines,
            minLines = minLines,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = WarmTextDark),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused }
                .clip(HealingShapes.Medium)
                .background(color = bgAlpha)
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            borderColor,
                            borderColor.copy(alpha = 0.5f)
                        )
                    ),
                    shape = HealingShapes.Medium
                ),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = HealingSpacing.Medium,
                            vertical = HealingSpacing.Small
                        )
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = WarmTextLight
                        )
                    }
                    Box(modifier = Modifier.padding(end = if (trailingIcon != null) 40.dp else 0.dp)) {
                        innerTextField()
                    }
                    trailingIcon?.let {
                        Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                            it()
                        }
                    }
                }
            }
        )
    }
}
