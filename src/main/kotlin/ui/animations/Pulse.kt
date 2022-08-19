package ui.animations

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
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

