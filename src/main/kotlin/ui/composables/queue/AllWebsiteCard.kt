package ui.composables.queue

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import ui.i18n.Lang
import viewmodel.queue.IAllEntriesViewModel
import viewmodel.queue.IQueueElementViewModel

@Composable
fun AllWebsiteCard(viewModel: IAllEntriesViewModel, actionBar: List<ActionBarElement> = listOf()) {
    val status by viewModel.status.collectAsState()
    val error by viewModel.lastErrorMessage.collectAsState()
    val lang by Lang.collectAsState()

    SimpleCard(
        viewModel = viewModel,
        actionBar = actionBar,
        title = lang.allEntriesVmCardTitle,
        author = viewModel.site.name,
        status = status,
        error = if (status == IQueueElementViewModel.Status.ERROR) error else null,
        website = viewModel.site
    )
}