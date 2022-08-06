package util.logs

import util.desktop.openFileInDefaultApp
import java.io.File
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy
fun openLogsFolder(): Unit {
    getCurrentLogFile()?.parentFile?.openFileInDefaultApp()
}