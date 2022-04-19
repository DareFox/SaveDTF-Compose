package ui.composables.queue

import androidx.compose.animation.animateColorAsState
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.Folder
import compose.icons.feathericons.Trash2
import logic.document.processors.downloadMediaAsync
import ui.SaveDtfTheme
import ui.viewmodel.queue.EntryQueueElementViewModel
import ui.viewmodel.queue.IEntryQueueElementViewModel
import ui.viewmodel.queue.IQueueElementViewModel
import ui.viewmodel.queue.IQueueElementViewModel.QueueElementStatus
import util.getMediaId
import util.getMediaIdOrNull
import java.awt.Desktop
import java.io.File

data class ActionBarElement(
    val icon: ImageVector,
    val description: String,
    val onClickCallback: (IQueueElementViewModel) -> Unit,
)

@Composable
@Preview
fun PreviewCard() {
    SaveDtfTheme(false) {
        QueueCard(
            viewModel = EntryQueueElementViewModel("a"),
            actionBar = listOf(ActionBarElement(FeatherIcons.Trash2, "delete") {})
        )
    }
}

@Composable
fun QueueCard(viewModel: IQueueElementViewModel, actionBar: List<ActionBarElement> = listOf()) {
    when (viewModel) {
        is IEntryQueueElementViewModel -> EntryCard(viewModel, actionBar)
    }
}

@Composable
fun EntryCard(viewModel: IEntryQueueElementViewModel, actionBar: List<ActionBarElement> = listOf()) {
    val entry by viewModel.entry.collectAsState()
    val status by viewModel.status.collectAsState()

    val title = if (entry == null) "Статья" else {
        val entryTitle = entry?.title

        if (entryTitle?.isEmpty() != false) {
            "Без названия"
        } else {
            entryTitle
        }
    }
    val author = entry?.author?.name ?: viewModel.url

    GenericCard(viewModel, actionBar, title, author, status)
}

@Composable
fun GenericCard(
    viewModel: IQueueElementViewModel,
    actionBar: List<ActionBarElement> = listOf(),
    title: String,
    author: String,
    status: QueueElementStatus,
    error: String? = null,
    painter: Painter? = null
) {
    val color by animateColorAsState(when (status) {
        QueueElementStatus.ERROR -> Color.Red.copy(0.7f)
        QueueElementStatus.WAITING_INIT -> Color.Gray.copy(0.8f)
        QueueElementStatus.READY_TO_USE -> Color.Gray.copy(0.00001f) // Composite over transparent color
        QueueElementStatus.SAVED -> Color.Green.copy(0.5f)
    })

    val onColor by animateColorAsState(when(status) {
        QueueElementStatus.WAITING_INIT -> Color.White
        else -> Color.Black
    })

    LaunchedEffect(Unit) {
        if (viewModel.status.value == QueueElementStatus.WAITING_INIT) {
            viewModel.initialize()
        }
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10))
            .shadow(8.dp)
            .fillMaxWidth()
    ) {
        Surface( // Main body
            modifier = Modifier
                .fillMaxWidth(),
            color = color.compositeOver(MaterialTheme.colors.primary)
        ) {
            val progress by viewModel.progress.collectAsState()

            Column(modifier = Modifier.fillMaxWidth().padding(start = 12.dp, bottom = 12.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.h2,
                    maxLines = 1,
                    color = onColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = author,
                    style = MaterialTheme.typography.subtitle2,
                    maxLines = 1,
                    fontStyle = FontStyle.Italic,
                    color = onColor
                )

                error?.let {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.subtitle2,
                        fontStyle = FontStyle.Italic,
                        color = onColor
                    )
                }

                progress?.let {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(progress ?: "", style = MaterialTheme.typography.subtitle2, color = onColor)
                }
            }

            // Add image, when painter is not null
            painter?.also {
                Image(
                    painter = it,
                    contentDescription = null,
                    contentScale = ContentScale.FillHeight,
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(
                            align = Alignment.TopEnd,
                            unbounded = true
                        ) // Make box to not constrain oversize
                        .size(150.dp) // Oversize card
                        .graphicsLayer { alpha = 0.9999f } // Workaround to enable alpha compositing
                        .drawWithContent {
                            val colors = listOf(
                                Color.Transparent,
                                Color.Black
                            )
                            drawContent()
                            drawRect(
                                brush = Brush.horizontalGradient(colors),
                                blendMode = BlendMode.DstIn
                            )
                        }
                )
            }
        }
        Surface( // Footer
            modifier = Modifier.height(45.dp).fillMaxWidth(),
            color = color.compositeOver(MaterialTheme.colors.primaryVariant),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val status by viewModel.status.collectAsState()
                    val buttons = actionBar.toMutableList()

                    if (status == QueueElementStatus.SAVED) {
                        buttons.add(ActionBarElement(FeatherIcons.Folder, "Open folder") {
                            Desktop.getDesktop().open(File(viewModel.pathToSave))
                        })
                    }

                    buttons.forEach {
                        Icon(it.icon, it.description, modifier = Modifier.clickable {
                            it.onClickCallback(viewModel)
                        }, tint = onColor)
                        Spacer(modifier = Modifier.fillMaxHeight().width(20.dp))
                    }
                }

//                val selected by viewModel.selected.collectAsState()
//
//                Checkbox(
//                    checked = selected,
//                    onCheckedChange = {
//                        if (it) {
//                            viewModel.select()
//                        } else {
//                            viewModel.unselect()
//                        }
//                    },
//                    colors = CheckboxDefaults.colors(uncheckedColor = onColor)
//                )
            }
        }
    }
}