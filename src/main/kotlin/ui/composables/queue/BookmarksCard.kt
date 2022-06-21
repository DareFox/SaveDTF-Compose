package ui.composables.queue

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import ui.viewmodel.queue.IBookmarksElementViewModel
import ui.viewmodel.queue.IQueueElementViewModel

@Composable
fun BookmarksCard(viewModel: IBookmarksElementViewModel, actionBar: List<ActionBarElement> = listOf()) {
    val status by viewModel.status.collectAsState()
    val error by viewModel.lastErrorMessage.collectAsState()
    val author = viewModel.site.name
    val title = "Bookmarks"

    SimpleCard(
        viewModel = viewModel,
        actionBar = actionBar,
        title = title,
        author = author,
        status = status,
        error = if (status == IQueueElementViewModel.QueueElementStatus.ERROR) error else null,
        website = viewModel.site
    )
}