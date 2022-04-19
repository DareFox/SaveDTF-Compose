package ui.composables

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import compose.icons.FeatherIcons
import compose.icons.feathericons.Info

@Composable
fun InfoPopup(title: String, text: String, onClose: () -> Unit) {
    Window(
        onCloseRequest = onClose,
        state = rememberWindowState(width = 550.dp, height = Dp.Unspecified),
        enabled = true,
        title = title,
        resizable = false,
        icon = painterResource("img/DTF_logo.png")

    ) {
        Surface(color = MaterialTheme.colors.background) {
            Column(modifier = Modifier.padding(25.dp, 10.dp).defaultMinSize(150.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(FeatherIcons.Info, null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = text,
                        style = MaterialTheme.typography.subtitle1
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                FancyButton(true, onClose, "OK")
            }
        }
    }
}