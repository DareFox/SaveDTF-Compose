package ui.composables

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
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
    buttonColors: ButtonColors = ButtonDefaults.buttonColors(),
) {
    FancyButton(
        enabled = enabled,
        onClick = onClick,
        buttonColors = buttonColors,
        placeholderButton = {
            Text(placeholderButton,
                style = MaterialTheme.typography.subtitle2,
                fontWeight = FontWeight.Bold)
        }
    )
}

@Composable
fun FancyButton(
    enabled: Boolean,
    onClick: () -> Unit,
    buttonColors: ButtonColors = ButtonDefaults.buttonColors(),
    placeholderButton: @Composable RowScope.() -> Unit,
) {
    Button(
        enabled = enabled,
        onClick = onClick,
        content = placeholderButton,
        modifier = Modifier.fillMaxWidth().defaultMinSize(Dp.Unspecified, 50.dp).clip(shape = RoundedCornerShape(25)),
        colors = buttonColors
    )
}