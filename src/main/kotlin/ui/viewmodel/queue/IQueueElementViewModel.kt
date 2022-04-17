package ui.viewmodel.queue

import kotlinx.coroutines.flow.StateFlow
import ui.ISelectable
import java.io.File

sealed interface IQueueElementViewModel: ISelectable {
    enum class QueueElementStatus {
        WAITING_INIT,
        ERROR,
        READY_TO_USE,
        SAVED
    }

    val status: StateFlow<QueueElementStatus>
    val lastErrorMessage: StateFlow<String?>
    val isDownloaded: StateFlow<Boolean>
    val progress: StateFlow<String?>

    suspend fun initialize()
    suspend fun save(folder: File): Boolean
    suspend fun download(): Boolean
}