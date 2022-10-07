package ui.composables.queue

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import shared.saveable.IBookmarksSaveable
import shared.saveable.ISaveable
import ui.i18n.Lang

@Composable
fun BookmarksCard(viewModel: IBookmarksSaveable, actionBar: List<ActionBarElement> = listOf()) {
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
        error = if (status == ISaveable.Status.ERROR) error else null,
        website = viewModel.site
    )
}