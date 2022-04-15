package ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import ui.composables.InfoBanner
import ui.composables.NavBarPreview
import ui.composables.TextBarElement
import ui.composables.TextNavBar
import ui.menus.QueueCreatorMenu

@Composable
fun AppUI() {
    Surface(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
        var navbarElement by remember { mutableStateOf(NavBar.DOWNLOAD_MENU) }
        var navbarIndex by remember { mutableStateOf(0) }

        Column {
            InfoBanner()
            Box(modifier = Modifier.weight(1f)) {
                when (navbarElement) {
                    NavBar.DOWNLOAD_MENU -> QueueCreatorMenu()
                    NavBar.SETTINGS_MENU -> NavBarPreview()
                }
            }
            Box {
                TextNavBar(listOf(
                    TextBarElement("Download") {
                        navbarIndex = it
                        navbarElement = NavBar.DOWNLOAD_MENU
                    },
                    TextBarElement("Settings") {
                        navbarIndex = it
                        navbarElement = NavBar.SETTINGS_MENU
                    },
                ), navbarIndex)
            }
        }
    }
}

private enum class NavBar() {
    DOWNLOAD_MENU,
    SETTINGS_MENU
}