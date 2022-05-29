package ui.viewmodel.queue

import exception.QueueElementException
import exception.errorOnNull
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
import logic.document.DocumentProcessor
import logic.document.modules.ImageDownloadModule
import logic.document.modules.QuoteAuthor
import logic.document.modules.VideoDownloadModule
import mu.KotlinLogging
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import ui.viewmodel.SettingsViewModel
import ui.viewmodel.queue.IQueueElementViewModel.QueueElementStatus.*
import util.UrlUtil
import util.convertToValidName
import java.io.File
import java.util.*

interface IEntryQueueElementViewModel : IQueueElementViewModel {
    val url: String
    val entry: StateFlow<Entry?>
}

private val logger = KotlinLogging.logger { }

data class EntryQueueElementViewModel(override val url: String) : AbstractElementViewModel(), IEntryQueueElementViewModel {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var document: Document? = null
    private val documentProcessor = MutableStateFlow<DocumentProcessor?>(null)

    private val _entry = MutableStateFlow<Entry?>(null)
    override val entry: StateFlow<Entry?> = _entry

    override val pathToSave: String?
        get() {
            return super.pathToSave?.let {
                val entry = entry.value

                val entryId = entry?.id ?: UUID.randomUUID().toString()
                val entryName = entry?.title ?: "no title"
                val entryFolder = convertToValidName("$entryId-$entryName", "$entryId-null")

                val authorId = entry?.author?.id ?: "unknown id"
                val authorName = entry?.author?.name ?: "unknown author"
                val authorFolder = convertToValidName("$authorId-$authorName", "$authorId-null")
                
                val folder = File(it)

                val pathToSave = folder.resolve("entry/$authorFolder/$entryFolder")
                pathToSave.absolutePath
            }
        }

    init {
        documentProcessor.onEach { // on entry downloader change
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
            try {
                _status.value = INITIALIZING
                _entry.value = null
                documentProcessor.value = null

                logger.info { "Parsing website" }
                val website = UrlUtil.getWebsiteType(url).errorOnNull("Website $url is not supported")

                val token = SettingsViewModel.tokens.value[website]
                val api = if (token != null) {
                    authKmtt(website, token)
                } else {
                    publicKmtt(website)
                }

                val entry = api.entry.getEntry(url)
                _entry.value = entry

                val html = entry.entryContent.errorOnNull("Entry content is null").html.errorOnNull("Entry html is null")
                document = Jsoup.parse(html)

                _status.value = READY_TO_USE
            } catch (ex: QueueElementException) {
                error(ex.errorMessage)
            } catch (ex: Exception) {
                error("[${ex.javaClass.name}]: ${ex.message}")
            }
        }
    }

    override suspend fun save(): Boolean {
        mutex.lock()

        try {
            yield()
            _status.value = IN_USE
            val path = pathToSave.errorOnNull("No path to save")
            val document = document.errorOnNull("Parsed document is null")

            val processor = DocumentProcessor(document, File(path))
            documentProcessor.value = processor

            yield()
            processor.removeCSS()
            processor.reformat()
            entry.value?.title?.also {
                processor.changeTitle(it)
            }

            yield()
            return try {
                processor.saveDocument(listOf(ImageDownloadModule, VideoDownloadModule, QuoteAuthor), 1, false)
                saved("Saved")
                true
            } catch (e: Exception) {
                error("[${e.javaClass.simpleName}]: ${e.message}")
                false
            }
        } catch (_: CancellationException) {
            error("Operation cancelled")
            return false
        } catch (ex: QueueElementException) {
            error(ex.errorMessage)
            return false
        }
        finally {
            mutex.unlock()
        }
    }
}
