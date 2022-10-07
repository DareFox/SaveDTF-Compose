package ui.composables.queue

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import shared.saveable.IEntrySaveable
import shared.saveable.ISaveable
import shared.i18n.Lang

@Composable
fun EntryCard(viewModel: IEntrySaveable, actionBar: List<ActionBarElement> = listOf()) {
    val entry by viewModel.entry.collectAsState()
    val status by viewModel.status.collectAsState()
    val error by viewModel.lastErrorMessage.collectAsState()
    val website by viewModel.website.collectAsState()
    val lang by shared.i18n.LangState.collectAsState()

    val title = if (entry == null) lang.entryCard else {
        val entryTitle = entry?.title

        if (entryTitle?.isEmpty() != false) {
            lang.entryNoTitle
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
        error = if (status == ISaveable.Status.ERROR) error else null,
        website = website
    )
}