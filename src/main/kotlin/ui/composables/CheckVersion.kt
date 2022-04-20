package ui.composables

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import logic.Version
import mu.KotlinLogging
import ui.viewmodel.AppViewModel
import ui.viewmodel.SettingsViewModel
import java.awt.Desktop
import java.net.URI

private val logger = KotlinLogging.logger { }

@Composable
fun CheckVersion(forced: Boolean = false) {
    var showPopup by remember { mutableStateOf(false) }
    var lastVersion: Version? by remember { mutableStateOf(null) }
    val ignoreUpdates by SettingsViewModel.ignoreUpdate.collectAsState()

    LaunchedEffect(Unit) {
        logger.info { "Checking updates" }

        lastVersion = AppViewModel.getLastVersionOrNull()

        logger.info { "GitHub version: $lastVersion" }
        logger.info { "Current version: ${AppViewModel.currentVersionObject}" }

        lastVersion?.let {
            if (AppViewModel.currentVersionObject < it && (forced || !ignoreUpdates)) {
                showPopup = true
            }
        }
    }

    if (showPopup) {
        InfoPopup(
            title = "Обновление SaveDTF",
            text = "Доступная новая версия $lastVersion\n(текущая версия: ${AppViewModel.currentVersionObject})",
            onClose = {
                showPopup = false
            }
        ) {
            Row {
                Surface(modifier = Modifier.weight(1f)) {
                    FancyButton(
                        enabled = true,
                        onClick = { showPopup = false }
                    ) {
                        Text("Напомнить потом", textAlign = TextAlign.Center)
                    }
                }
                Surface(modifier = Modifier.padding(horizontal = 10.dp).weight(1f)) {
                    FancyButton(
                        enabled = true,
                        onClick = {
                            showPopup = false
                            SettingsViewModel.setIgnoreUpdate(true)
                        }
                    ) {
                        Text("Отключить проверку", textAlign = TextAlign.Center)
                    }
                }
                Surface(modifier = Modifier.weight(1f)) {
                    FancyButton(true, {
                        showPopup = false
                        Desktop.getDesktop().browse(URI.create(AppViewModel.latestVersionURL))
                    }) {
                        Text("Скачать")
                    }
                }
            }
        }
    }
}