package ui.viewmodel.queue

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ui.viewmodel.SettingsViewModel
import ui.viewmodel.queue.IQueueElementViewModel.QueueElementStatus

/**
 * Abstract class that implements default fields of [IQueueElementViewModel]
 */
abstract class AbstractElementViewModel : IQueueElementViewModel {
    protected val _status = MutableStateFlow(QueueElementStatus.INITIALIZING)
    override val status: StateFlow<QueueElementStatus> = _status

    protected val _lastErrorMessage = MutableStateFlow<String?>(null)
    override val lastErrorMessage: StateFlow<String?> = _lastErrorMessage

    protected val _progress = MutableStateFlow<String?>(null)
    override val progress: StateFlow<String?> = _progress

    protected var customPath: String? = null
    override val pathToSave: String?
        get() = customPath ?: SettingsViewModel.folderToSave.value

    override fun setPathToSave(folder: String) {
        customPath = folder
    }

    protected val _selected = MutableStateFlow(false)
    override val selected: StateFlow<Boolean> = _selected

    override fun select() {
        _selected.value = true
    }

    override fun unselect() {
        _selected.value = false
    }

    protected fun error(message: String) {
        _lastErrorMessage.value = message
        _status.value = QueueElementStatus.ERROR
    }

    protected fun saved(message: String) {
        _lastErrorMessage.value = null
        _status.value = QueueElementStatus.SAVED
    }
}