import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import logic.Version
import ui.AppUI
import ui.SaveDtfTheme
import ui.composables.CheckVersion
import ui.composables.InfoPopup

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "SaveDTF!",
        state = rememberWindowState(size = DpSize(820.dp, 740.dp)),
        icon = painterResource("img/DTF_logo.png"),
    ) {
        SaveDtfTheme(true) {
            AppUI()
            CheckVersion()
        }
    }
}
