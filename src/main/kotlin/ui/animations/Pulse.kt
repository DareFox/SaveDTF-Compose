package ui.animations

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

@Composable
fun pulseColor(
    initColor: Color = Color.Transparent,
    targetColor: Color,
    timeBetweenStatesMs: Int = 1500
): State<Color> {
    val infiniteTransition = rememberInfiniteTransition()

    return infiniteTransition.animateColor(
        initColor, targetColor, InfiniteRepeatableSpec(
            animation = tween(timeBetweenStatesMs),
            repeatMode = RepeatMode.Reverse
        )
    )
}

