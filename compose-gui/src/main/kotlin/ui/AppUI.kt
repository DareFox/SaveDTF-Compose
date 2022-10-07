package ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import ui.composables.InfoBanner
import ui.composables.TextBarElement
import ui.composables.TextNavBar
import shared.i18n.Lang
import ui.menus.QueueCreatorMenu
import ui.menus.SettingsMenu

@Composable
fun AppUI() {
    Surface(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
        val lang by shared.i18n.LangState.collectAsState()
        var navbarElement by remember { mutableStateOf(NavBar.DOWNLOAD_MENU) }
        var navbarIndex by remember { mutableStateOf(0) }

        Column {
            InfoBanner()
            Box(modifier = Modifier.weight(1f)) {
                when (navbarElement) {
                    NavBar.DOWNLOAD_MENU -> QueueCreatorMenu()
                    NavBar.SETTINGS_MENU -> SettingsMenu()
                }
            }
            Box {
                TextNavBar(
                    listOf(
                        TextBarElement(lang.navBarMenuDownload) {
                            navbarIndex = it
                            navbarElement = NavBar.DOWNLOAD_MENU
                        },
                        TextBarElement(lang.navBarMenuSettings) {
                            navbarIndex = it
                            navbarElement = NavBar.SETTINGS_MENU
                        },
                    ), navbarIndex
                )
            }
        }
    }
}

private enum class NavBar {
    DOWNLOAD_MENU,
    SETTINGS_MENU
}