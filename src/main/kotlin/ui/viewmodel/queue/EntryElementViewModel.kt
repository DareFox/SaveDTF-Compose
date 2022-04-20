package ui.viewmodel.queue

import kmtt.impl.authKmtt
import kmtt.impl.publicKmtt
import kmtt.models.entry.Entry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.yield
import logic.downloaders.IEntryDownloader
import logic.downloaders.entryDownloader
import mu.KotlinLogging
import ui.viewmodel.SettingsViewModel
import ui.viewmodel.queue.IQueueElementViewModel.QueueElementStatus
import util.UrlUtil
import util.convertToValidName
import java.io.File
import java.util.*

interface IEntryQueueElementViewModel : IQueueElementViewModel {
    val url: String
    val entry: StateFlow<Entry?>
}

private val logger = KotlinLogging.logger { }

data class EntryQueueElementViewModel(override val url: String) : IEntryQueueElementViewModel {
    private val scope = CoroutineScope(Dispatchers.Default)

    private var entryDownloader = MutableStateFlow<IEntryDownloader?>(null)

    private val _entry = MutableStateFlow<Entry?>(null)
    override val entry: StateFlow<Entry?> = _entry

    private val _status = MutableStateFlow(QueueElementStatus.INITIALIZING)
    override val status: StateFlow<QueueElementStatus> = _status

    private val _lastErrorMessage = MutableStateFlow<String?>(null)
    override val lastErrorMessage: StateFlow<String?> = _lastErrorMessage

    private val _progress = MutableStateFlow<String?>(null)
    override val progress: StateFlow<String?> = _progress

    private val savePath = SettingsViewModel.folderToSave
    private var userPath: String? = null

    override val pathToSave: String?
        get() {
            return (userPath ?: savePath.value)?.let {
                val entry = entry.value

                val entryId = entry?.id ?: UUID.randomUUID().toString()
                val entryName = entry?.title ?: "no title"
                val entryFolder = convertToValidName("$entryId-$entryName")

                val authorId = entry?.author?.id ?: "unknown id"
                val authorName = entry?.author?.name ?: "unknown author"
                val authorFolder = convertToValidName("$authorId-$authorName")
                
                val folder = File(it)

                val pathToSave = folder.resolve("entry/$authorFolder/$entryFolder")
                pathToSave.absolutePath
            }
        }


    init {
        entryDownloader.onEach { // on entry downloader change
            if (it == null) { // if no downloader -> no progress
                logger.info { "No downloader, no progress" }
                _progress.value = null
            } else {
                logger.info { "Downloader exists, listening to progress" }
                it.progress.onEach { progress -> // on progress of downloader change
                    _progress.value = progress
                }.launchIn(scope = scope)
            }
        }.launchIn(scope = scope)
    }

    private val mutex = Mutex()

    override suspend fun initialize() {
        mutex.withLock {
            _status.value = QueueElementStatus.INITIALIZING
            _entry.value = null
            entryDownloader.value = null

            logger.info { "Parsing website" }
            val website = UrlUtil.getWebsiteType(url)

            if (website == null) {
                _lastErrorMessage.value = "Website $url is not supported"
                _status.value = QueueElementStatus.ERROR
                return
            }

            val token = SettingsViewModel.tokens.value.get(website)
            val api = if (token != null) {
                authKmtt(website, token)
            } else {
                publicKmtt(website)
            }

            val entry: Entry

            try {
                entry = api.entry.getEntry(url)
            } catch (ex: Exception) {
                _lastErrorMessage.value = ex.message
                _status.value = QueueElementStatus.ERROR
                return
            }

            _entry.value = entry
            entryDownloader.value =
                entryDownloader(entry, SettingsViewModel.retryAmount.value, SettingsViewModel.replaceErrorMedia.value)
            _status.value = QueueElementStatus.READY_TO_USE
        }
    }

    override suspend fun save(): Boolean {
        mutex.lock()

        try {
            yield()
            _status.value = QueueElementStatus.IN_USE
            val downloader = entryDownloader.value


            if (downloader == null) {
                _lastErrorMessage.value = "Initialize element before saving"
                _status.value = QueueElementStatus.ERROR
                return false
            }

            yield()
            val path = pathToSave
            if (path == null) {
                _lastErrorMessage.value = "No path to save"
                _status.value = QueueElementStatus.ERROR
                return false
            }


            yield()
            val downloaded: Boolean = if (!downloader.isDownloaded.value) {
                downloader.download()
            } else {
                true
            }

            yield()
            if (!downloaded) {
                _lastErrorMessage.value = "Can't download entry"
                _status.value = QueueElementStatus.ERROR
                return false
            }


            val folder = File(path)

            yield()
            val value = try {
                downloader.save(folder)
                _status.value = QueueElementStatus.SAVED
                true
            } catch (ex: Exception) {
                _lastErrorMessage.value = "$[{ex.javaClass.name}] ${ex.message}"
                _status.value = QueueElementStatus.ERROR
                logger.error {
                    ex.toString()
                }
                false
            }
            yield()
            return value
        } catch (_: CancellationException) {
            _lastErrorMessage.value = "Operation cancelled"
            _status.value = QueueElementStatus.ERROR
            return false
        } finally {
            mutex.unlock()
        }
    }

    private val _selected = MutableStateFlow(false)
    override val selected: StateFlow<Boolean> = _selected

    override fun select() {
        _selected.value = true
    }

    override fun unselect() {
        _selected.value = false
    }

    override fun setPathToSave(folder: String) {
        userPath = folder
    }
}
