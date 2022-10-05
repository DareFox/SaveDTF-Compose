package ui.composables.queue

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import ui.i18n.Lang
import shared.util.kmttapi.KmttUrl
import viewmodel.queue.IPeriodEntriesViewModel
import viewmodel.queue.IQueueElementViewModel

@Composable
fun PeriodEntriesCard(viewModel: IPeriodEntriesViewModel, actionBar: List<ActionBarElement> = listOf()) {
    val status by viewModel.status.collectAsState()
    val error by viewModel.lastErrorMessage.collectAsState()
    val url = viewModel.periodSitemapLink
    val lang by Lang.collectAsState()

    val period = KmttUrl.extractPeriodAndFormat(url) ?: KmttUrl.extractPeriod(url) ?: url

    SimpleCard(
        viewModel = viewModel,
        actionBar = actionBar,
        title = lang.periodEntriesVmCardTitle.format(period),
        author = viewModel.site.name,
        status = status,
        error = if (status == IQueueElementViewModel.Status.ERROR) error else null,
        website = viewModel.site
    )
}