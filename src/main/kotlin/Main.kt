import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import ui.AppUI
import ui.SaveDtfTheme
import ui.composables.CheckVersion
import ui.composables.InfoPopupColumn
import ui.i18n.Lang
import ui.menus.NotificationsUI
import util.desktop.openUrl
import util.getCrashLogReport
import java.awt.Toolkit
import java.awt.Window
import java.awt.datatransfer.StringSelection
import java.awt.event.WindowEvent

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    application {
        CompositionLocalProvider(
            LocalWindowExceptionHandlerFactory provides object : WindowExceptionHandlerFactory {
                override fun exceptionHandler(window: Window) = WindowExceptionHandler {
                    CrashMenu(it as Exception)
                    window.dispatchEvent(WindowEvent(window, WindowEvent.WINDOW_CLOSING))
                    throw it
                }
            }
        ) {
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
    }
}

// TODO: Doesn't work, fix it again
private fun CrashMenu(ex: Exception) {
    var show by mutableStateOf(true)
    val log = getCrashLogReport(ex)

    fun copyLogToClipboard() {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(StringSelection(log), null)
    }

    val actions = mutableListOf<Pair<String, () -> Unit>>().also {
        it.add(Lang.value.crashReportCopyLogsAndReport to {
            copyLogToClipboard()
            openUrl(Lang.value.crashReportURL)
        })

        it.add(Lang.value.crashReportCopyLogs to ::copyLogToClipboard)
    }

    application {
        SaveDtfTheme(true) {
            if (show) {
                InfoPopupColumn(Lang.value.crashReportTitle, Lang.value.crashReportDescription, {
                    show = false
                }, actions)
            }
        }
    }
}