package shared.util.logs

import shared.util.desktop.openFileInDefaultApp

fun openLogsFolder(): Unit {
    val folder = getCurrentLogFile()?.parentFile ?: return

    folder.mkdirs()
    folder.openFileInDefaultApp()
}