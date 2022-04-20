package ui.menus

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.Download
import compose.icons.feathericons.RefreshCcw
import compose.icons.feathericons.Trash
import kotlinx.coroutines.launch
import ui.SaveDtfTheme
import ui.composables.FancyButton
import ui.composables.FancyInputField
import ui.composables.queue.QueueList
import ui.viewmodel.SettingsViewModel
import ui.viewmodel.queue.IQueueElementViewModel.QueueElementStatus
import ui.viewmodel.queue.QueueViewModel

@Composable
@Preview
fun QueueCreatorMenuPreview() {
    SaveDtfTheme(true) {
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun QueueCreatorMenu() {
    val queueVM = QueueViewModel
    val queue by queueVM.queue.collectAsState()

    Surface(Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(10.dp, 0.dp)) {

            var input by rememberSaveable { mutableStateOf("") }
            var enable by rememberSaveable { mutableStateOf(false) }

            val hasFolder by SettingsViewModel.folderToSave.collectAsState()
            val supported = queueVM.canCreateQueueElement(input)
            val errorMessage = when {
                hasFolder == null -> "Установите папку для сохранений в настройках"
                input.isNotEmpty() && !supported -> "Неверный URL"
                else -> null
            }

            enable = errorMessage == null

            FancyInputField(
                onInputChange = {
                    input = it
                },
                onConfirm = {
                    queueVM.createAndAddQueueElement(it)
                    input = ""
                },
                input = input,
                placeholderInput = "Вставь ссылку сюда",
                enabled = enable,
                isError = errorMessage != null,
                errorMessage = errorMessage
            )
            Spacer(modifier = Modifier.fillMaxWidth().height(10.dp))

            val isQueueNotEmpty = queue.isNotEmpty()
            val scope = rememberCoroutineScope()

            val buttons = mutableListOf<@Composable () -> Unit>(
                {
                    // Download all button
                    FancyButton(isQueueNotEmpty, onClick = {
                        queue.forEach {
                            scope.launch {
                                if (it.status.value != QueueElementStatus.IN_USE) {
                                    it.save()
                                }
                            }
                        }
                    }) {
                        Icon(FeatherIcons.Download, null)
                    }
                },
                {
                    // Remove all from queue
                    FancyButton(isQueueNotEmpty, onClick = {
                        queueVM.clear()
                    }) {
                        Icon(FeatherIcons.Trash, null)
                    }
                },
                {
                    // Update all elements
                    FancyButton(isQueueNotEmpty, onClick = {
                        queueVM.queue.value.forEach {
                            scope.launch {
                                if (it.status.value != QueueElementStatus.IN_USE) {
                                    it.initialize()
                                }
                            }
                        }
                    }) {
                        Icon(FeatherIcons.RefreshCcw, null)
                    }
                }
            )

            Row {
                Surface(modifier = Modifier.weight(1f)) {
                    FancyButton(enable, onClick = {
                        queueVM.createAndAddQueueElement(input)
                        input = ""
                    }, "Добавить в очередь")
                }
                buttons.forEach {
                    // add buttons
                    Surface(modifier = Modifier.padding(start = 10.dp).width(50.dp)) {
                        it()
                    }
                }
            }


            QueueList()
        }

    }
}