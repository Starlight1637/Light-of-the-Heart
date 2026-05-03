package com.mindful.companion.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.mindful.companion.ui.theme.*

@Composable
fun EmptyState(
    title: String,
    description: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    emoji: String = "🌸",
    modifier: Modifier = Modifier
) {
    val emojiScale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        emojiScale.animateTo(
            1f,
            animationSpec = spring(
                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                stiffness = androidx.compose.animation.core.Spring.StiffnessLow
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(HealingSpacing.XXLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.graphicsLayer { scaleX = emojiScale.value; scaleY = emojiScale.value }
        )

        Spacer(modifier = Modifier.height(HealingSpacing.Large))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = WarmTextDark,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(HealingSpacing.XSmall))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = WarmTextLight,
            textAlign = TextAlign.Center
        )

        if (actionText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(HealingSpacing.XLarge))
            HealingPrimaryButton(
                text = actionText,
                onClick = onActionClick,
                modifier = Modifier.fillMaxWidth(0.6f)
            )
        }
    }
}
