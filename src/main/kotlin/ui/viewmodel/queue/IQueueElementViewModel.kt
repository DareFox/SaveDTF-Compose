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
    val progress: StateFlow<String?>
    val pathToSave: String

    suspend fun initialize()
    suspend fun save(): Boolean
    fun setPathToSave(folder: String)
}