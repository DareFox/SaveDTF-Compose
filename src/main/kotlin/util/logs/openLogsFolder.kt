package util.logs

import util.desktop.openFileInDefaultApp
import java.io.File

fun openLogsFolder(): Unit {
    val path = System.getProperty("user.dir") ?: return

    File(path).resolve("logs").openFileInDefaultApp()
}