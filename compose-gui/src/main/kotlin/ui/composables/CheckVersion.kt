package ui.composables

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import shared.Version
import mu.KotlinLogging
import ui.i18n.Lang
import viewmodel.AppViewModel
import viewmodel.SettingsViewModel
import java.awt.Desktop
import java.net.URI

private val logger = KotlinLogging.logger { }

@Composable
fun CheckVersion(forced: Boolean = false) {
    val lang by Lang.collectAsState()
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
            title = lang.updateSaveDTF,
            text = lang.updateMessage.format(lastVersion, AppViewModel.currentVersionObject),
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
                        Text(lang.updateRemindLater, textAlign = TextAlign.Center)
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
                        Text(lang.updateTurnOffAutoCheck, textAlign = TextAlign.Center)
                    }
                }
                Surface(modifier = Modifier.weight(1f)) {
                    FancyButton(true, {
                        showPopup = false
                        Desktop.getDesktop().browse(URI.create(AppViewModel.latestVersionURL))
                    }) {
                        Text(lang.updateDownload)
                    }
                }
            }
        }
    }
}