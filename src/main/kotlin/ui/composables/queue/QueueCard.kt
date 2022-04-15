package ui.composables.queue

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.Trash2
import ui.SaveDtfTheme
import ui.viewmodel.queue.EntryQueueElementViewModel
import ui.viewmodel.queue.IEntryQueueElementViewModel
import ui.viewmodel.queue.IQueueElementViewModel
import ui.viewmodel.queue.IQueueElementViewModel.*

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
            viewmodel = EntryQueueElementViewModel("a"),
            actionBar = listOf(ActionBarElement(FeatherIcons.Trash2, "delete") {})
        )
    }
}

@Composable
fun QueueCard(viewmodel: IQueueElementViewModel, actionBar: List<ActionBarElement> = listOf()) {
    when (viewmodel) {
        is IEntryQueueElementViewModel -> EntryCard(viewmodel, actionBar)
    }
}

@Composable
fun EntryCard(viewmodel: IEntryQueueElementViewModel, actionBar: List<ActionBarElement> = listOf()) {
    val entry by viewmodel.entry.collectAsState()
    val status by viewmodel.status.collectAsState()

    val title = if (entry == null) "Статья" else {
        val entryTitle = entry?.title

        if (entryTitle?.isEmpty() != false) {
            "Без названия"
        } else {
            entryTitle
        }
    }
    val author = entry?.author?.name ?: viewmodel.url

    GenericCard(viewmodel, actionBar, title, author, status)
}

@Composable
fun GenericCard(
    viewmodel: IQueueElementViewModel,
    actionBar: List<ActionBarElement> = listOf(),
    title: String,
    author: String,
    status: QueueElementStatus,
    error: String? = null
) {
    val color = when (status) {
        QueueElementStatus.ERROR -> Color.Red.copy(0.7f)
        QueueElementStatus.WAITING_INIT -> Color.Gray.copy(0.8f)
        QueueElementStatus.READY_TO_USE -> Color.Gray.copy(0.00001f)
        QueueElementStatus.SAVED -> Color.Green.copy(0.8f)
    }

    LaunchedEffect(Unit) {
        viewmodel.initialize()
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(topStartPercent = 10, bottomStartPercent = 10))
            .shadow(8.dp)
            .fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier
                .requiredHeightIn(max = if (error == null) 85.dp else 150.dp)
                .fillMaxWidth(),
            color = color.compositeOver(MaterialTheme.colors.primary)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(start = 12.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.h2,
                    color = MaterialTheme.colors.onPrimary,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = author,
                    style = MaterialTheme.typography.subtitle1,
                    maxLines = 1,
                    color = MaterialTheme.colors.onPrimary,
                    fontStyle = FontStyle.Italic,
                )
                if (error != null) {
                    Text(
                        text = error ?: "null",
                        style = MaterialTheme.typography.subtitle1,
                        color = MaterialTheme.colors.onPrimary,
                        fontStyle = FontStyle.Italic,
                    )
                }
            }

            Image(
                painter = painterResource("img/hehe.webp"),
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
        Surface(
            modifier = Modifier.height(45.dp).fillMaxWidth(),
            color = color.compositeOver(MaterialTheme.colors.primaryVariant),
        ) {
            Row(
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                actionBar.forEach {
                    Icon(it.icon, it.description, modifier = Modifier.clickable {
                        it.onClickCallback(viewmodel)
                    })
                    Spacer(modifier = Modifier.fillMaxHeight().width(20.dp))
                }
            }
        }
    }
}