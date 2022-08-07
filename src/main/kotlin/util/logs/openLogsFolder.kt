package util.logs

import util.desktop.openFileInDefaultApp

fun openLogsFolder(): Unit {
    getCurrentLogFile()?.parentFile?.openFileInDefaultApp()
}