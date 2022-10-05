package shared.util.desktop

import java.awt.Desktop
import java.io.File

fun File.openFileInDefaultApp() {
    val os = System.getProperty("os.name").lowercase()
    when (os) {
        "windows" -> {
            val process = ProcessBuilder("cmd.exe", "/c", this.canonicalPath)
            process.start()
        }
        "linux" -> {
            val process = ProcessBuilder("xdg-open", this.canonicalPath)
            process.start()
        }
        else -> {
            Desktop.getDesktop().open(this)
        }
    }
}