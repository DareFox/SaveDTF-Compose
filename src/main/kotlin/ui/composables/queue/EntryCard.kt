package ui.composables.queue

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import ui.viewmodel.queue.IEntryQueueElementViewModel
import ui.viewmodel.queue.IQueueElementViewModel

@Composable
fun EntryCard(viewModel: IEntryQueueElementViewModel, actionBar: List<ActionBarElement> = listOf()) {
    val entry by viewModel.entry.collectAsState()
    val status by viewModel.status.collectAsState()
    val error by viewModel.lastErrorMessage.collectAsState()
    val website by viewModel.website.collectAsState()

    val title = if (entry == null) "Статья" else {
        val entryTitle = entry?.title

        if (entryTitle?.isEmpty() != false) {
            "Без названия"
        } else {
            entryTitle
        }
    }
    val author = entry?.author?.name ?: viewModel.url

    SimpleCard(
        viewModel = viewModel,
        actionBar = actionBar,
        title = title,
        author = author,
        status = status,
        error = if (status == IQueueElementViewModel.QueueElementStatus.ERROR) error else null,
        website = website
    )
}