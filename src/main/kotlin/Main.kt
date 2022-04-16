import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ui.AppUI
import ui.SaveDtfTheme

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "SaveDTF!",
        icon = painterResource("img/hehe.webp"),
    ) {
        SaveDtfTheme(true) {
            AppUI()
        }
    }
}
