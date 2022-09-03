package viewmodel.queue

import exception.QueueElementException
import exception.errorOnNull
import kmtt.impl.authKmtt
import kmtt.impl.publicKmtt
import kmtt.models.entry.Entry
import kmtt.models.enums.Website
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import logic.document.SettingsBasedDocumentProcessor
import mu.KotlinLogging
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import ui.i18n.Lang
import viewmodel.SettingsViewModel
import util.filesystem.convertToValidName
import util.kmttapi.UrlUtil
import util.progress.redirectTo
import java.io.File
import java.util.*

interface IEntryQueueElementViewModel : IQueueElementViewModel {
    val url: String
    val entry: StateFlow<Entry?>
    val website: StateFlow<Website?>
}

private val logger = KotlinLogging.logger { }

data class EntryQueueElementViewModel(override val url: String) : AbstractElementViewModel(),
    IEntryQueueElementViewModel {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var document: Document? = null
    private val documentProcessor = MutableStateFlow<SettingsBasedDocumentProcessor?>(null)

    private val _website = MutableStateFlow<Website?>(null)
    override val website: StateFlow<Website?> = _website

    private val _entry = MutableStateFlow<Entry?>(null)
    override val entry: StateFlow<Entry?> = _entry


    override val pathToSave: String?
        get() { // TODO: ??????????????? Simplify!
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
                logger.debug { "No downloader, no progress" }
                clearProgress()
            } else {
                logger.debug { "Downloader exists, listening to progress" }
                // on progress of downloader change
                it.redirectTo(mutableProgress, scope)
            }
        }.launchIn(scope = scope)
    }

    private val mutex = Mutex()

    override suspend fun initialize() {
        mutex.withLock {
            try {
                initializing()
                _entry.value = null
                documentProcessor.value = null

                logger.info { "Parsing website" }
                progress(Lang.value.entryElementVmParsingLink)
                val website = UrlUtil.getWebsiteType(url).errorOnNull(Lang.value.entryQueueElementVmUrlNotSupported.format(url))
                _website.value = website

                val token = SettingsViewModel.tokens.value[website]

                val api = if (token != null) {
                    authKmtt(website, token)
                } else {
                    publicKmtt(website)
                }

                progress(Lang.value.entryElementVmRequestingEntry)
                val entry = api.entry.getEntry(url)
                _entry.value = entry

                val html = entry.entryContent.errorOnNull("Entry content is null").html.errorOnNull("Entry html is null")
                progress(Lang.value.entryElementVmParsingEntryHtml)
                document = Jsoup.parse(html)

                clearProgress()
                readyToUse()
            } catch (ex: QueueElementException) {
                error(ex.errorMessage)
            } catch (ex: Throwable) {
                error(ex)
            }
        }
    }

    override suspend fun save(): Deferred<Boolean> {
       return waitAndAsyncJob {
           mutex.lock()
           try {
               inUse()
               val path = pathToSave.errorOnNull("No path to save")
               val document = document.errorOnNull("Parsed document is null")

               val processor = SettingsBasedDocumentProcessor(File(path), document, entry.value)
               documentProcessor.value = processor

               yield()
               processor.process()

               saved()
               clearProgress()
               true
           } catch (_: CancellationException) {
               error(Lang.value.entryQueueElementVmOperationCancelled)
               false
           } catch (ex: QueueElementException) {
               error(ex.errorMessage)
               false
           } catch (ex: Throwable) {
               error(ex)
               false
           }
           finally {
               mutex.unlock()
           }
       }
    }
}
