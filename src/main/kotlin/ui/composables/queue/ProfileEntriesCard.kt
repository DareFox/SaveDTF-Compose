package ui.composables.queue

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import ui.viewmodel.queue.IBookmarksElementViewModel
import ui.viewmodel.queue.IProfileElementViewModel
import ui.viewmodel.queue.IQueueElementViewModel

@Composable
fun ProfileCard(viewModel: IProfileElementViewModel, actionBar: List<ActionBarElement> = listOf()) {
    val status by viewModel.status.collectAsState()
    val error by viewModel.lastErrorMessage.collectAsState()
    val author by viewModel.user.collectAsState()
    val title = "All profile entries"

    SimpleCard(
        viewModel = viewModel,
        actionBar = actionBar,
        title = title,
        author = author?.name ?: "null",
        status = status,
        error = if (status == IQueueElementViewModel.QueueElementStatus.ERROR) error else null,
        website = viewModel.site
    )
}