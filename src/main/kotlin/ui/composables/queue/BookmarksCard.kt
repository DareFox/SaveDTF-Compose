package ui.composables.queue

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import ui.i18n.Lang
import ui.viewmodel.queue.IBookmarksElementViewModel
import ui.viewmodel.queue.IQueueElementViewModel

@Composable
fun BookmarksCard(viewModel: IBookmarksElementViewModel, actionBar: List<ActionBarElement> = listOf()) {
    val lang by Lang.collectAsState()
    val status by viewModel.status.collectAsState()
    val error by viewModel.lastErrorMessage.collectAsState()
    val author = viewModel.site.name

    SimpleCard(
        viewModel = viewModel,
        actionBar = actionBar,
        title = lang.bookmarksCard,
        author = author,
        status = status,
        error = if (status == IQueueElementViewModel.QueueElementStatus.ERROR) error else null,
        website = viewModel.site
    )
}