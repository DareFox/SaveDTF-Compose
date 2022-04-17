package ui.viewmodel.queue

import kmtt.impl.authKmtt
import kmtt.impl.publicKmtt
import kmtt.models.entry.Entry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import logic.downloaders.IEntryDownloader
import logic.downloaders.entryDownloader
import logic.downloaders.exceptions.NoContentDownloadedException
import mu.KotlinLogging
import ui.viewmodel.SettingsViewModel
import ui.viewmodel.queue.IQueueElementViewModel.QueueElementStatus
import util.UrlUtil
import java.io.File

interface IEntryQueueElementViewModel : IQueueElementViewModel {
    val url: String
    val entry: StateFlow<Entry?>
}

private val logger = KotlinLogging.logger { }

data class EntryQueueElementViewModel(override val url: String): IEntryQueueElementViewModel {
    private val scope = CoroutineScope(Dispatchers.Default)

    private var entryDownloader = MutableStateFlow<IEntryDownloader?>(null)

    private val _entry = MutableStateFlow<Entry?>(null)
    override val entry: StateFlow<Entry?> = _entry

    private val _status = MutableStateFlow(QueueElementStatus.WAITING_INIT)
    override val status: StateFlow<QueueElementStatus> = _status

    private val _lastErrorMessage = MutableStateFlow<String?>(null)
    override val lastErrorMessage: StateFlow<String?> = _lastErrorMessage

    private val _isDownloaded = MutableStateFlow(false)
    override val isDownloaded: StateFlow<Boolean> = _isDownloaded

    private val _progress = MutableStateFlow<String?>(null)
    override val progress: StateFlow<String?> = _progress

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

    override suspend fun initialize() {
        _status.value = QueueElementStatus.WAITING_INIT
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
        entryDownloader.value = entryDownloader(entry, SettingsViewModel.retryAmount.value, SettingsViewModel.replaceErrorMedia.value)
        _status.value = QueueElementStatus.READY_TO_USE
    }

    override suspend fun save(folder: File): Boolean {
        val downloader = entryDownloader.value

        requireNotNull(downloader) {
            "Initialize element before saving"
        }

        return try {
            _status.value = QueueElementStatus.READY_TO_USE
            downloader.save(folder)
            _status.value = QueueElementStatus.SAVED
            true
        } catch (e: NoContentDownloadedException) {
            _lastErrorMessage.value = "Download Entry, before saving it"
            _status.value = QueueElementStatus.ERROR
            false
        }
    }

    override suspend fun download(): Boolean {
        _status.value = QueueElementStatus.READY_TO_USE
        val downloader = entryDownloader.value

        requireNotNull(downloader) {
            "Initialize element before saving"
        }

        val result = downloader.download()

        return if (result) {
            _status.value = QueueElementStatus.READY_TO_USE
            _isDownloaded.value = downloader.isDownloaded.value
            true
        } else {
            _status.value = QueueElementStatus.ERROR
            _lastErrorMessage.value = "Can't download entry"
            false
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
}