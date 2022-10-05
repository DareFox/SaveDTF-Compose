package shared.util.desktop

import java.awt.Desktop
import java.net.URI

fun openUrl(url: String) {
    Desktop.getDesktop().browse(URI.create(url))
}