package shared.saveable

import exception.OperationTimeoutException
import exception.QueueElementException
import exception.errorOnNull
import i18n.langs.LanguageResource
import kmtt.models.entry.Entry
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import mu.KotlinLogging
import org.jsoup.Jsoup
import shared.document.DocumentProcessor
import shared.document.IDocumentProcessor
import shared.document.IProcessorOperation
import shared.i18n.Lang
import shared.saveable.ISaveable.*
import shared.util.filesystem.toDirectory
import shared.util.kmttapi.KmttUrl
import shared.util.kmttapi.betterPublicKmtt
import shared.util.progress.redirectTo
import java.io.File

/**
 * Abstract class of [ISaveable] that implements all interface methods and fields.
 *
 * This class provides functions for easier
 * class constructing such as [saveImpl], [initializeImpl], [timeoutOperation] and etc
 *
 * @param func Leave empty lambda `{}`. It's needed to infer class name for logger
 *
 * @see saveImpl
 * @see initializeImpl
 * @see timeoutOperation
 * @see setStatus
 * @see setProgress
 * @see setSavedTo
 */
abstract class AbstractSaveable(
    emptyLambda: () -> Unit,
    protected val apiTimeoutInSeconds: Int,
    protected val entryTimeoutInSeconds: Int,
    folderToSave: File,
    protected val operations: Set<IProcessorOperation>,
) : ISaveable {
    private val internalLogger = KotlinLogging.logger { }
    protected val logger = KotlinLogging.logger(emptyLambda)

    /**
     * Get current [SettingsViewModel.folderToSave].
     *
     * @throws QueueElementException if [SettingsViewModel.folderToSave] is null
     * */
    protected val baseSaveFolder: File = folderToSave

    protected val lang: LanguageResource
        get() = Lang

    protected var ioScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    protected val mutex = Mutex()

    protected val _savedTo = MutableStateFlow<String?>(null)
    override val savedTo: StateFlow<String?>
        get() = _savedTo

    protected val _status = MutableStateFlow(Status.INITIALIZING)
    override val status: StateFlow<Status>
        get() = _status

    protected val _lastErrorMessage = MutableStateFlow<String?>(null)
    override val lastErrorMessage: StateFlow<String?>
        get() = _lastErrorMessage

    protected val _progress = MutableStateFlow<String?>(null)
    override val progress: StateFlow<String?>
        get() = _progress

    protected val _currentJob = MutableStateFlow<Job?>(null)
    override val currentJob: StateFlow<Job?>
        get() = _currentJob

    override fun initializeAsync(): Deferred<Throwable?>? {
        return when {
            mutex.tryLock() -> ioScope.async {
                try {
                    resetPathAndMessages()
                    setStatus(Status.INITIALIZING)
                    val result = catchAndPrint { initializeImpl() }

                    if (result == null) {
                        setStatus(Status.READY_TO_USE)
                    }

                    result
                } finally {
                    mutex.unlock()
                }
            }.also { handleJob(it) }
            else -> null
        }
    }

    /**
     * Implementation of [initializeAsync]. This method will have exception handling,
     * meaning exception will be caught, printed to [lastErrorMessage] and operation will be cancelled
     */
    abstract suspend fun initializeImpl()

    override fun saveAsync(): Deferred<Throwable?>? {
        return when {
            mutex.tryLock() -> ioScope.async {
                try {
                    resetPathAndMessages()
                    setStatus(Status.IN_USE)
                    val catch = catchAndPrint { saveImpl() }

                    if (catch == null) {
                        setStatus(Status.SAVED)
                    }

                    catch
                } finally {
                    mutex.unlock()
                }
            }.also { handleJob(it) }
            else -> null
        }
    }

    /**
     * Implementation of [saveAsync]. This method will have exception handling,
     * meaning exception will be caught, printed to [lastErrorMessage] and operation will be cancelled
     */
    abstract suspend fun saveImpl()

    /**
     * Run suspending block and catch exceptions.
     *
     * If function caught exception, it will print error to [lastErrorMessage],
     * set status of element to [ERROR][ISaveable.Status.ERROR] and
     * cancel current operation
     */
    private suspend fun catchAndPrint(tryBlock: suspend () -> Unit): Throwable? {
        return try {
            tryBlock()
            null
        } catch (ex: Throwable) {
            setErrorMessageWithStatus(ex)
            ex
        }
    }

    /**
     * Handles job cancellation and adding to [currentJob] field
     */
    private fun handleJob(job: Job) {
        _currentJob.value = job

        job.invokeOnCompletion { error ->
            _currentJob.value = null

            if (error?.cause is CancellationException || error is CancellationException) {
                resetPathAndMessages()

                val job = initializeAsync()
                runBlocking {
                    job?.await()
                }

                return@invokeOnCompletion
            }

            if (error != null) {
                setErrorMessageWithStatus(error)
            }
        }
    }

    /**
     * Retrieve, process and save entry to `%parentDir%/ID-AUTHORNAME/ID-ENTRYNAME`
     */
    protected suspend fun processEntry(url: String, parentDir: File, currentCounter: Int) {
        val apiTimeout = apiTimeoutInSeconds * 1000L

        val newCounter = currentCounter + 1
        val prefix = "${Lang.queueVmEntry} #${newCounter}"

        val site = KmttUrl.getWebsiteType(url)
        requireNotNull(site) {
            "Can't get website type from $url url"
        }

        internalLogger.debug { "Getting entry from osnova api" }
        setProgress("$prefix, ${Lang.abstractElementVmRequestingEntry.format(url)}")

        val entry = if (apiTimeout <= 0) {
            betterPublicKmtt(site).entry.getEntry(url)
        } else {
            timeoutOperation(apiTimeout, lang.withTimeoutApiRequestOperation) {
                betterPublicKmtt(site).entry.getEntry(url)
            }
        }

        return processEntry(entry, parentDir, currentCounter)
    }

    /**
     * Try to retrieve, process and save entry to `%parentDir%/ID-AUTHORNAME/ID-ENTRYNAME`
     *
     * On success, returns **true**.
     *
     * Else, returns **false**.
     */
    protected suspend fun tryProcessEntry(
        url: String,
        parentDir: File,
        currentCounter: Int
    ): Boolean {
        return try {
            processEntry(url, parentDir, currentCounter)
            true
        } catch (ex: Throwable) {
            logger.error(ex) { "Failed to process $url" }
            false
        }
    }

    /**
     * Process and save entry to `%parentDir%/ID-AUTHORNAME/ID-ENTRYNAME`
     */
    protected suspend fun processEntry(
        entry: Entry,
        parentDir: File,
        currentCounter: Int
    ) {
        val entryMs = entryTimeoutInSeconds * 1000L

        val document = entry
            .entryContent
            .errorOnNull("Entry content is null")
            .html
            .errorOnNull("Entry html is null")
            .let { Jsoup.parse(it) } // parse document

        val processor = DocumentProcessor(document, entry, entry.toDirectory(parentDir), operations)

        val counter = processor.redirectTo(_progress, ioScope) {// redirect progress of processor to this VM progress
            val progressValue = it?.run { ", $this" } ?: ""
            // show entry counter
            if (currentJob.value?.isCancelled != true) "${Lang.queueVmEntry} #${currentCounter + 1}$progressValue"
            // show nothing on cancellation
            else null
        }

        internalLogger.debug { "Starting entry processing with id ${entry.id}" }
        if (entryMs <= 0) {
            processor.process()
            counter.cancel()
        } else {
            timeoutOperation(
                timeout = entryMs,
                name = lang.withTimeoutEntryProcessingOperation,
                finallyBlock = {
                    counter.cancel()
                }
            ) {
                processor.process()
            }
        }
        internalLogger.debug { "Finished processing entry with id ${entry.id}" }
    }

    /**
     * Try to process and save entry to `%parentDir%/ID-AUTHORNAME/ID-ENTRYNAME`
     *
     * On success, returns **true**.
     *
     * Else, returns **false**.
     */
    protected suspend fun tryProcessEntry(
        entry: Entry,
        parentDir: File,
        currentCounter: Int
    ): Boolean {
        return try {
            processEntry(entry, parentDir, currentCounter)
            true
        } catch (ex: Throwable) {
            logger.error(ex) { "Failed to process entry with ${entry.id} id" }
            false
        }
    }

    /**
     * Set error message to argument and change status to [Status.ERROR]
     */
    protected fun setErrorMessageWithStatus(message: String) {
        setErrorMessage(message)
        setStatus(Status.ERROR)
    }

    /**
     * Set error message to argument.
     * Status will not change
     */
    protected fun setErrorMessage(message: String) {
        _lastErrorMessage.update { message }
    }

    /**
     * Set error message from exception.
     *
     * If error is [CancellationException], element will be set to [READY_TO_USE][Status.READY_TO_USE] status.
     *
     * If error is [QueueElementException] and it's [message][Throwable.message] is not null, then only this message will be printed. Otherwise,
     * it will be print error as usual
     */
    protected fun setErrorMessageWithStatus(ex: Throwable) {
        if (ex.cause is CancellationException || ex is CancellationException) {
            setErrorMessageWithStatus(lang.entryQueueElementVmOperationCancelled)
            return
        }


        val errorName = ex::class.simpleName
        val message = ex.message

        if (ex is QueueElementException) {
            if (!message.isNullOrBlank()) {
                setErrorMessageWithStatus(message)
            } else {
                setErrorMessageWithStatus("Error! $errorName")
            }
            return
        }

        if (!message.isNullOrBlank()) {
            setErrorMessageWithStatus("Error! $errorName: $message")
        } else {
            setErrorMessageWithStatus("Error! $errorName")
        }
    }

    /**
     * Clear last error message.
     *
     * **IT DOES NOT REMOVE ERROR [STATUS][ISaveable.status]**
     */
    protected fun removeErrorMessage() {
        _lastErrorMessage.value = null

    }

    /**
     * Set progress message
     */
    protected fun setProgress(progress: String) {
        _progress.value = progress
    }

    /**
     * Clear progress
     */
    protected fun removeProgress() {
        _progress.value = null
    }

    /**
     * Set status of element
     *
     * Values: [Status]
     */
    protected fun setStatus(status: Status) {
        _status.update { status }
    }

    /**
     * Removes ["saved to" path][setSavedTo], last [progress] and [error message][lastErrorMessage]
     */
    protected fun resetPathAndMessages() {
        removeProgress()
        removeErrorMessage()
        removeSavedPath()
    }

    /**
     * Print result based on [errorList]
     *
     * If errorList is not empty, then his content will be called in logger and [QueueElementException] will be thrown
     */
    protected fun showResult(
        errorList: List<String>,
        counter: Int,
        savedTo: String
    ) {
        val errorCounter = errorList.count()

        when {
            errorCounter > 0 -> {
                removeProgress()
                logger.error("Failed to download:\n${errorList.joinToString(",\n")}")

                throw QueueElementException(Lang.genericElementVmAllErrorsWithLogs.format(counter, errorCounter))
            }
            errorCounter == counter -> {
                logger.error("Failed to download:\n${errorList.joinToString(",\n")}")
                removeProgress()

                throw QueueElementException(Lang.genericElementVmSomeErrorsWithLogs.format(errorCounter))
            }
            else -> {
                setProgress(Lang.genericElementVmNoErrors.format(counter))
                setSavedTo(savedTo)
            }
        }
    }

    /**
     * Runs a given suspending [block] of code inside
     * a coroutine with a specified timeout
     *
     * Internally wraps [withTimeout] and converts [TimeoutCancellationException] to [OperationTimeoutException]
     *
     * @throws OperationTimeoutException if the timeout was exceeded
     */
    protected suspend fun <T> timeoutOperation(
        timeout: Long,
        name: String,
        finallyBlock: () -> Unit = {},
        block: suspend CoroutineScope.() -> T
    ): T {
        try {
            return withTimeout(timeout, block)
        } catch (ex: TimeoutCancellationException) {
            throw OperationTimeoutException(timeout, name)
        } finally {
            finallyBlock()
        }
    }

    /**
     * Set ["saved to"][savedTo] path
     */
    protected fun setSavedTo(path: String) {
        _savedTo.value = path
    }

    /**
     * Remove ["saved to"][savedTo] path and set null
     */
    protected fun removeSavedPath() {
        _savedTo.value = null
    }
}