package ui.composables.queue

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import shared.saveable.IAllEntriesSaveable
import shared.saveable.ISaveable
import shared.i18n.Lang
@Composable
fun AllWebsiteCard(viewModel: IAllEntriesSaveable, actionBar: List<ActionBarElement> = listOf()) {
    val status by viewModel.status.collectAsState()
    val error by viewModel.lastErrorMessage.collectAsState()
    val lang by shared.i18n.LangState

    SimpleCard(
        viewModel = viewModel,
        actionBar = actionBar,
        title = lang.allEntriesVmCardTitle,
        author = viewModel.site.name,
        status = status,
        error = if (status == ISaveable.Status.ERROR) error else null,
        website = viewModel.site
    )
}