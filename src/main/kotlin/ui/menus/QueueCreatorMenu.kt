package ui.menus

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.*
import kmtt.models.enums.Website
import kotlinx.coroutines.launch
import ui.SaveDtfTheme
import ui.composables.FancyButton
import ui.composables.FancyInputField
import ui.composables.queue.QueueList
import ui.i18n.Lang
import viewmodel.NotificationData
import viewmodel.NotificationType
import viewmodel.NotificationsViewModel
import viewmodel.SettingsViewModel
import viewmodel.queue.AllEntriesViewModel
import viewmodel.queue.IQueueElementViewModel.Status
import viewmodel.queue.QueueViewModel

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
            val lang by Lang.collectAsState()
            val errorMessage = when {
                hasFolder == null -> lang.queueCreatorFolderError
                input.isNotEmpty() && !supported -> lang.queueCreatorInvalidURL
                else -> null
            }

            enable = errorMessage == null


            val isQueueNotEmpty = queue.isNotEmpty()
            val scope = rememberCoroutineScope()

            var showBookmarksMenu by rememberSaveable { mutableStateOf(false) }
            var showArchiveMenu by rememberSaveable { mutableStateOf(false) }

            val bookmarkIcon = if (showBookmarksMenu) {
                FeatherIcons.ChevronRight
            } else {
                FeatherIcons.Bookmark
            }

            val archiveIcon = if (showArchiveMenu) {
                FeatherIcons.ChevronRight
            } else {
                FeatherIcons.Archive
            }

            Row(Modifier.height(50.dp)) {
                /* search field */
                Surface(modifier = Modifier.weight(1f)) {
                    FancyInputField(
                        onInputChange = {
                            input = it
                        },
                        onConfirm = {
                            queueVM.createAndAddQueueElement(it)
                            input = ""
                        },
                        input = input,
                        placeholderInput = lang.queueCreatorPlaceholder,
                        enabled = enable,
                        isError = errorMessage != null,
                        errorMessage = errorMessage
                    )

                }

                if (showArchiveMenu) {
                    /* create bookmark element with associated website */
                    Website.values().forEach { website ->
                        Surface(modifier = Modifier.padding(start = 10.dp).width(50.dp)) {
                            FancyButton(true, onClick = {
                                QueueViewModel.add(AllEntriesViewModel(website))
                            }) {
                                Image(getPainterByWebsite(website), null)
                            }
                        }
                    }
                }


                /* archive button */
                Surface(modifier = Modifier.padding(start = 10.dp).width(50.dp)) {
                    // Show/hide website selector
                    FancyButton(true, onClick = {
                        showArchiveMenu = !showArchiveMenu
                    }) {
                        Icon(archiveIcon, null)
                    }
                }



                if (showBookmarksMenu) {
                    /* create bookmark element with associated website */
                    Website.values().forEach { website ->
                        val canCreateBookmark = QueueViewModel.canCreateBookmarks(website)

                        Surface(modifier = Modifier.padding(start = 10.dp).width(50.dp)) {
                            FancyButton(canCreateBookmark, onClick = {
                                QueueViewModel.createBookmarks(website).let { QueueViewModel.add(it) }
                            }, onDisabledClick = {
                                NotificationsViewModel.add(
                                    NotificationData(
                                    text = lang.queueCreatorNoTokenError.format(website),
                                    type = NotificationType.ERROR,
                                    onScreenDuration = 5
                                )
                                )
                            }) {
                                Image(getPainterByWebsite(website), null)
                            }
                        }
                    }
                }

                /* bookmarks button */
                Surface(modifier = Modifier.padding(start = 10.dp).width(50.dp)) {
                    // Show/hide website selector
                    FancyButton(true, onClick = {
                        showBookmarksMenu = !showBookmarksMenu
                    }) {
                        Icon(bookmarkIcon, null)
                    }
                }



            }
            Spacer(modifier = Modifier.fillMaxWidth().height(10.dp))

            val buttons = mutableListOf<@Composable () -> Unit>(
                {
                    // Download all button
                    FancyButton(isQueueNotEmpty, onClick = {
                        queue.forEach {
                            scope.launch {
                                if (Status.READY_TO_USE == it.status.value) {
                                    it.saveAsync()
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
                                if (it.status.value != Status.IN_USE) {
                                    it.initializeAsync()
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
                    }, lang.queueCreatorAddToQueue)
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

@Composable
private fun getPainterByWebsite(website: Website): Painter {
    return when (website) {
        Website.DTF -> painterResource("img/icons/dtf.svg")
        Website.TJ -> painterResource("img/icons/tj.png")
        Website.VC -> painterResource("img/icons/vcru.svg")
    }
}