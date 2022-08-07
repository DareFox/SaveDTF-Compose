package ui.composables.queue

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import ui.i18n.Lang
import ui.viewmodel.queue.IProfileElementViewModel
import ui.viewmodel.queue.IQueueElementViewModel

@Composable
fun ProfileCard(viewModel: IProfileElementViewModel, actionBar: List<ActionBarElement> = listOf()) {
    val lang by Lang.collectAsState()
    val status by viewModel.status.collectAsState()
    val error by viewModel.lastErrorMessage.collectAsState()
    val author by viewModel.user.collectAsState()
    val immutableAuthor = author

    val title = if (immutableAuthor != null) {
        lang.profileElementVmCardTitleWithName.format(immutableAuthor.name)
    } else {
        lang.profileElementVmCardTitleWithID.format(viewModel.id)
    }

    SimpleCard(
        viewModel = viewModel,
        actionBar = actionBar,
        title = title,
        author = viewModel.site.name,
        status = status,
        error = if (status == IQueueElementViewModel.QueueElementStatus.ERROR) error else null,
        website = viewModel.site
    )
}