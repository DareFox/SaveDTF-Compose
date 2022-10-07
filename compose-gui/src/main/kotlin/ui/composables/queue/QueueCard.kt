package ui.composables.queue

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.Download
import compose.icons.feathericons.Folder
import compose.icons.feathericons.RefreshCcw
import compose.icons.feathericons.XOctagon
import kmtt.models.enums.Website
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import shared.saveable.*
import shared.saveable.ISaveable.Status
import ui.animations.pulseColor
import ui.i18n.Lang
import shared.util.desktop.openFileInDefaultApp
import java.io.File

data class ActionBarElement(
    val icon: ImageVector,
    val description: String,
    val onClickCallback: (ISaveable) -> Unit,
)

@Composable
fun QueueCard(viewModel: ISaveable, actionBar: List<ActionBarElement> = listOf()) {
    when (viewModel) {
        is IBookmarksSaveable -> BookmarksCard(viewModel, actionBar)
        is IEntrySaveable -> EntryCard(viewModel, actionBar)
        is IProfileSaveable -> ProfileCard(viewModel, actionBar)
        is IPeriodEntriesSaveable -> PeriodEntriesCard(viewModel, actionBar)
        is IAllEntriesSaveable -> AllWebsiteCard(viewModel, actionBar)
        else -> {}
    }
}

@Composable
fun SimpleCard(
    viewModel: ISaveable,
    actionBar: List<ActionBarElement> = listOf(),
    title: String,
    author: String,
    status: Status,
    website: Website?,
    error: String? = null,
    painter: Painter? = null,
    pulseOnUse: Boolean = true,
) {
    /**
     * Indicator to start pulse animation
     */
    val pulse = when {
        // set pulse only if feature was on (pulseOnUse == true)
        pulseOnUse -> {
            when (status) {
                // on usage set start for pulse animation
                Status.IN_USE, Status.INITIALIZING -> true
                else -> false
            }
        }
        else -> false
    }

    /**
     *  Color which infinitely with pulse animation transforms between two colors
     */
    val colorPulse by animateColorAsState(
        if (pulse) {
            // start pulse animation
            val color by pulseColor(targetColor = Color.White.copy(0.19f))
            color
        } else {
            // no pulse -> use Transparent color
            Color.Transparent
        }
    )

    /**
     * Color associated with website. Used when status of element is ready_to_use or in_use
     */
    val websiteColor = when (website) {
        Website.DTF -> MaterialTheme.colors.primary
        Website.TJ -> Color(0xFFffd260)
        Website.VC -> Color(0xFFe55c78)
        else -> Color.Gray
    }

    /**
     * Main color. (duh)
     */
    val mainColor by animateColorAsState(
        colorPulse.compositeOver(
            when (status) {
                Status.ERROR -> Color(0xFFd53333)
                Status.INITIALIZING -> Color(0xFF808080)
                Status.READY_TO_USE, Status.IN_USE -> websiteColor
                Status.SAVED -> Color(0xFF37ee9a)
            }
        )
    )


    /**
     * Footer color. A little darker than the main color
     */
    val secondaryColor by animateColorAsState(
        colorPulse
            .compositeOver(
                Color.Black.copy(0.15f)
                    .compositeOver(mainColor)
            )
    )

    /**
     * Text and icons color
     */
    val onColor by animateColorAsState(
        when (status) {
            Status.INITIALIZING -> Color.White
            else -> Color.Black
        }
    )

    QueueCard(
        viewModel = viewModel,
        actionBar = actionBar,
        title = title,
        author = author,
        status = status,
        mainColor = mainColor,
        footerColor = secondaryColor,
        error = error,
        painter = painter,
        onColor = onColor
    )
}

@Composable
fun QueueCard(
    viewModel: ISaveable,
    actionBar: List<ActionBarElement> = listOf(),
    title: String,
    author: String,
    status: Status,
    mainColor: Color,
    footerColor: Color,
    onColor: Color,
    error: String? = null,
    painter: Painter? = null,
) {

    LaunchedEffect(Unit) {
        if (viewModel.status.value == Status.INITIALIZING) {
            viewModel.initializeAsync()
        }
    }

    val path by viewModel.savedTo.collectAsState()

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10))
            .shadow(8.dp)
            .fillMaxWidth()
    ) {
        /**
         * Main body
         */
        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            color = mainColor
        ) {
            val progress by viewModel.progress.collectAsState()

            Column(modifier = Modifier.fillMaxWidth().padding(start = 12.dp, bottom = 12.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.h3,
                    maxLines = 1,
                    color = onColor,
                    modifier = Modifier.padding(top = 10.dp)
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

        /**
         * Footer
         */
        Surface(
            modifier = Modifier.height(45.dp).fillMaxWidth(),
            color = footerColor,
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
                    val scope = rememberCoroutineScope {
                        Dispatchers.IO
                    }

                    val buttons = actionBar.toMutableList()
                    val lang by Lang.collectAsState()
                    val job by viewModel.currentJob.collectAsState()

                    if (job?.isActive == true) {
                        buttons.add(ActionBarElement(FeatherIcons.XOctagon, "STOP") {
                            scope.launch {
                                job?.cancel()

                                // re-initialize to get original document
                                // TODO: In abstract class, undo document changes on cancellation without reinitialization
                                viewModel.initializeAsync()
                            }
                        })
                    }

                    if (status == Status.SAVED && path != null) {
                        buttons.add(ActionBarElement(FeatherIcons.Folder, lang.queueCardOpen) {
                            File(path!!).openFileInDefaultApp()
                        })
                    }

                    if (status in listOf(Status.READY_TO_USE)) {
                        buttons += ActionBarElement(FeatherIcons.Download, lang.queueCardSave) {
                            scope.launch(CoroutineName("Save operation coroutine")) {
                                if (status != Status.IN_USE) { // Double check
                                    it.saveAsync()
                                }
                            }
                        }
                    }

                    if (status != Status.INITIALIZING && job?.isActive != true) {
                        buttons += ActionBarElement(FeatherIcons.RefreshCcw, lang.queueCardRefresh) {
                            scope.launch(CoroutineName("Init operation coroutine")) {
                                it.initializeAsync()
                            }
                        }
                    }

                    buttons.forEach {
                        Icon(it.icon, it.description, modifier = Modifier.clickable {
                            it.onClickCallback(viewModel)
                        }, tint = onColor)
                        Spacer(modifier = Modifier.fillMaxHeight().width(20.dp))
                    }
                }

                // TODO: Add selection mode
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