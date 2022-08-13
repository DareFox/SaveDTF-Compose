package ui.viewmodel.queue

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import logic.abstracts.AbstractProgress
import mu.KotlinLogging
import ui.viewmodel.SettingsViewModel
import ui.viewmodel.queue.IQueueElementViewModel.QueueElementStatus
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Abstract class that implements default fields of [IQueueElementViewModel]
 */
abstract class AbstractElementViewModel : IQueueElementViewModel, AbstractProgress() {
    protected val _status = MutableStateFlow(QueueElementStatus.INITIALIZING)
    override val status: StateFlow<QueueElementStatus> = _status

    protected val ioScope = CoroutineScope(Dispatchers.IO)

    protected val _currentJob = MutableStateFlow<Job?>(null)
    override val currentJob: StateFlow<Job?> = _currentJob

    protected val _lastErrorMessage = MutableStateFlow<String?>(null)
    override val lastErrorMessage: StateFlow<String?> = _lastErrorMessage

    protected var customPath: String? = null
    override val pathToSave: String?
        get() = customPath ?: SettingsViewModel.folderToSave.value

    override fun setPathToSave(folder: String) {
        customPath = folder
    }

    /**
     * Use it to restrict multiple parallel executions
     */
    protected val elementMutex = Mutex()

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

    protected fun inUse() {
        _status.value = QueueElementStatus.IN_USE
    }

    protected fun error(message: String) {
        _lastErrorMessage.value = message
        _status.value = QueueElementStatus.ERROR
    }

    protected inline fun error(exception: Throwable) {
        _lastErrorMessage.value = "[${exception.javaClass.simpleName}] ${exception.message ?: ""}"
        _status.value = QueueElementStatus.ERROR

        // TODO: Use child class in logger, not abstract
        val klogger = KotlinLogging.logger {  }

        klogger.error {
            "Error() log:\n\n${exception.stackTraceToString()}"
        }
    }

    protected fun saved() {
        _lastErrorMessage.value = null
        _status.value = QueueElementStatus.SAVED
    }
    protected suspend fun waitAndLaunchJob(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        _currentJob.value?.join()

        val job = ioScope.launch(
            context, start, block
        )
        _currentJob.value = job

        job.invokeOnCompletion {
            handleCancellation(it)
        }

        return job
    }

    protected suspend fun <T> waitAndAsyncJob(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> T
    ): Deferred<T> {
        _currentJob.value?.join()

        val job = ioScope.async(
            context, start, block
        )
        _currentJob.value = job

        job.invokeOnCompletion {
            handleCancellation(it)
        }

        return job
    }

    private fun handleCancellation(error: Throwable?) {
        if (error?.cause is CancellationException || error is CancellationException) {
            readyToUse()
            clearProgress()
        }

        if (error != null) {
            error(error)
        }
    }
}