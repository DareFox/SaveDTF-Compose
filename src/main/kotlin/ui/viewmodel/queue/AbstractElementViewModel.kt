package ui.viewmodel.queue

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import logic.abstracts.AbstractProgress
import ui.viewmodel.SettingsViewModel
import ui.viewmodel.queue.IQueueElementViewModel.QueueElementStatus

/**
 * Abstract class that implements default fields of [IQueueElementViewModel]
 */
abstract class AbstractElementViewModel : IQueueElementViewModel, AbstractProgress() {
    protected val _status = MutableStateFlow(QueueElementStatus.INITIALIZING)
    override val status: StateFlow<QueueElementStatus> = _status

    protected val _lastErrorMessage = MutableStateFlow<String?>(null)
    override val lastErrorMessage: StateFlow<String?> = _lastErrorMessage

    protected var customPath: String? = null
    override val pathToSave: String?
        get() = customPath ?: SettingsViewModel.folderToSave.value

    override fun setPathToSave(folder: String) {
        customPath = folder
    }

    protected val mutexInitializer = Mutex()

    protected val _selected = MutableStateFlow(false)
    override val selected: StateFlow<Boolean> = _selected

    override fun select() {
        _selected.value = true
    }

    override fun unselect() {
        _selected.value = false
    }

    protected fun initializing() {
        _status.value = QueueElementStatus.INITIALIZING
    }

    protected fun readyToUse() {
        _status.value = QueueElementStatus.READY_TO_USE
    }

    protected fun error(message: String) {
        _lastErrorMessage.value = message
        _status.value = QueueElementStatus.ERROR
    }

    protected fun saved() {
        _lastErrorMessage.value = null
        _status.value = QueueElementStatus.SAVED
    }
}