package viewmodel.queue

import exception.errorOnNull
import kmtt.models.entry.Entry
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import logic.abstracts.AbstractProgress
import logic.document.SettingsBasedDocumentProcessor
import mu.KLogger
import mu.KotlinLogging
import org.jsoup.Jsoup
import ui.i18n.Lang
import util.coroutine.cancelOnSuspendEnd
import util.filesystem.toDirectory
import util.kmttapi.UrlUtil
import util.kmttapi.betterPublicKmtt
import util.progress.redirectTo
import viewmodel.SettingsViewModel
import viewmodel.queue.IQueueElementViewModel.QueueElementStatus
import java.io.File
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Abstract class that implements default fields of [IQueueElementViewModel]
 */
abstract class AbstractElementViewModel : IQueueElementViewModel, AbstractProgress() {
    protected val _status = MutableStateFlow(QueueElementStatus.INITIALIZING)
    override val status: StateFlow<QueueElementStatus> = _status

    protected val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

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

    protected suspend fun tryProcessDocument(url: String, parentDir: File, currentCounter: Int, logger: KLogger = KotlinLogging.logger {  }): Boolean {
        return try {
            val newCounter = currentCounter+1
            val prefix = "${Lang.value.queueVmEntry} #${newCounter}"

            val site = UrlUtil.getWebsiteType(url)
            requireNotNull(site) {
                "Can't get website type from $url url"
            }

            logger.debug { "Getting entry from osnova api" }
            progress("$prefix, Getting entry $url from API")

            val entry = betterPublicKmtt(site).entry.getEntry(url)

            tryProcessDocument(entry, parentDir, currentCounter, logger)
        } catch (ex: Exception) {
            logger.error(ex) {
                "Failed to process document $url"
            }
            false
        }
    }
    protected suspend fun tryProcessDocument(entry: Entry, parentDir: File, currentCounter: Int, logger: KLogger = KotlinLogging.logger {}): Boolean {
        return try {
            val document = entry
                .entryContent
                .errorOnNull("Entry content is null")
                .html
                .errorOnNull("Entry html is null")
                .let { Jsoup.parse(it) } // parse document

            val processor = SettingsBasedDocumentProcessor(entry.toDirectory(parentDir), document, entry)

            processor
                .redirectTo(mutableProgress, ioScope) {// redirect progress of processor to this VM progress
                    val progressValue = it?.run { ", $this" } ?: ""

                    // show entry counter
                    if (currentJob.value?.isCancelled != true) "${Lang.value.queueVmEntry} #${currentCounter + 1}$progressValue"
                    // show nothing on cancellation
                    else null
                }
                .cancelOnSuspendEnd {
                    logger.debug { "Starting entry processing with id ${entry.id}" }
                    processor.process() // save document
                    logger.debug { "Finished processing entry with id ${entry.id}" }
                }

            true
        } catch (ex: Exception) { // on error, change result to false
            logger.error(ex) {
                "Failed to process ${entry.id}"
            }
            false
        }
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