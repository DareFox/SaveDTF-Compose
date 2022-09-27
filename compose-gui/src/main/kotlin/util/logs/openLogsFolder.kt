package util.logs

import util.desktop.openFileInDefaultApp

fun openLogsFolder(): Unit {
    val folder = getCurrentLogFile()?.parentFile ?: return

    folder.mkdirs()
    folder.openFileInDefaultApp()
}