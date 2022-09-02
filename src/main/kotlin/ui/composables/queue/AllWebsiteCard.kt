package ui.composables.queue

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import ui.i18n.Lang
import viewmodel.queue.AllEntriesViewModel
import viewmodel.queue.IProfileElementViewModel
import viewmodel.queue.IQueueElementViewModel

@Composable
fun AllWebsiteCard(viewModel: AllEntriesViewModel, actionBar: List<ActionBarElement> = listOf()) {
    val status by viewModel.status.collectAsState()
    val error by viewModel.lastErrorMessage.collectAsState()

    SimpleCard(
        viewModel = viewModel,
        actionBar = actionBar,
        title = "All entries",
        author = viewModel.site.name,
        status = status,
        error = if (status == IQueueElementViewModel.QueueElementStatus.ERROR) error else null,
        website = viewModel.site
    )
}