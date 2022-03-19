import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ui.*
import ui.composables.InfoBanner
import ui.composables.Menu

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "SaveDTF!",
        icon = painterResource("img/hehe.webp"),
    ) {
        SaveDtfTheme(true) {
            Surface(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
                Column {
                    InfoBanner()
                    Menu()
                }
            }
        }
    }
}
