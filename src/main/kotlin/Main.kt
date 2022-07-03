import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ui.AppUI
import ui.SaveDtfTheme
import ui.composables.CheckVersion
import ui.composables.InfoPopup
import ui.composables.InfoPopupColumn
import ui.menus.NotificationsUI
import util.desktop.openUrl
import util.getCrashLogReport
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

fun main() {
    try {
        application {
            Window(
                onCloseRequest = ::exitApplication,
                title = "SaveDTF!",
                state = rememberWindowState(size = DpSize(820.dp, 740.dp)),
                icon = painterResource("img/DTF_logo.png"),
            ) {
                SaveDtfTheme(true) {
                    AppUI()
                    NotificationsUI()
                    CheckVersion()
                }
            }
        }
    } catch(ex: Exception) {
        var show by mutableStateOf(true)
        val log = getCrashLogReport(ex)

        fun copyLogToClipboard() {
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            clipboard.setContents(StringSelection(log), null)
        }

        val actions = mutableListOf<Pair<String, () -> Unit>>().also {
            it.add("Copy logs and report issue to GitHub" to {
                copyLogToClipboard()
                openUrl("https://github.com/DareFox/SaveDTF-Compose/issues/new?assignees=&labels=bug&template=bug_report.yml&title=%5BBUG%5D+%2ATitle+of+issue%2A%0A")
            })

            it.add("Copy logs" to ::copyLogToClipboard)
        }

        application {
            SaveDtfTheme(true) {
                if (show) {
                    InfoPopupColumn("CRASH (⊙﹏⊙′)", "Program has been crashed. Would you like to report it?", {
                        show = false
                    }, actions)
                }
            }
        }
    }
}
