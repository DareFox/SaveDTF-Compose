package ui.composables

import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.layout.AnchorPane
import javafx.stage.DirectoryChooser

// Why JavaFX?
// 1) AWT doesn't have method for getting DIRECTORIES, not files
// 2) Swing have method for getting them, but it's very ugly because of cross-platform GUI
// JavaFX solves these two problems: getting directories and creating native OS GUI for "Open Folder" menu

private val fxPane = JFXPanel()

fun directoryDialog(
    windowTitle: String,
    onCloseRequest: (path: String?) -> Unit,
) {
    Platform.runLater {
        val anchorPane = AnchorPane()
        fxPane.scene = Scene(anchorPane)

        val dirChooser = DirectoryChooser()
        dirChooser.title = windowTitle

        val selected = dirChooser.showDialog(fxPane.scene.window)
        onCloseRequest(selected?.absolutePath)
    }
}