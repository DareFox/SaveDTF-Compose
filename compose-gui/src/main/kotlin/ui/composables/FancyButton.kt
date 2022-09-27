package ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun FancyButton(
    enabled: Boolean,
    onClick: () -> Unit,
    placeholderButton: String,
    onDisabledClick: () -> Unit = {},
    buttonColors: ButtonColors = ButtonDefaults.buttonColors(),
) {
    FancyButton(
        enabled = enabled,
        onClick = onClick,
        buttonColors = buttonColors,
        placeholderButton = {
            Text(
                placeholderButton,
                style = MaterialTheme.typography.subtitle2,
                fontWeight = FontWeight.Bold
            )
        },
        onDisabledClick = onDisabledClick
    )
}

@Composable
fun FancyButton(
    enabled: Boolean,
    onClick: () -> Unit,
    buttonColors: ButtonColors = ButtonDefaults.buttonColors(),
    onDisabledClick: () -> Unit = {},
    placeholderButton: @Composable RowScope.() -> Unit,
) {
    /* Use IntrinsicSize.Min to clip box inside surface */
    Surface(Modifier.height(IntrinsicSize.Min).fillMaxWidth()) {
        Button(
            enabled = enabled,
            onClick = onClick,
            content = placeholderButton,
            modifier = Modifier.fillMaxWidth().defaultMinSize(Dp.Unspecified, 50.dp)
                .clip(shape = RoundedCornerShape(25)),
            colors = buttonColors,
        )
        if (!enabled) {
            /* When button is disabled, hijack clicks from disabled button
               to invisible box (which have exact shape as button)

               Why don't use box/surface with button in it?
               Because even if button is disabled, it will consume clicks
               So we need to "hijack" them from button by placing transparent box on top of it
            */
            Box(Modifier.fillMaxHeight().fillMaxWidth().clip(shape = RoundedCornerShape(25)).clickable {
                onDisabledClick()
            })
        }
    }
}