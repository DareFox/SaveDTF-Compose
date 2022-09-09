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
import util.kmttapi.betterPublicKmtt
import util.progress.redirectTo
import java.io.File
import java.util.*

interface IEntryQueueElementViewModel : IQueueElementViewModel {
    val url: String
    val entry: StateFlow<Entry?>
    val website: StateFlow<Website?>
}

data class EntryQueueElementViewModel(override val url: String) : AbstractElementViewModel({}),
    IEntryQueueElementViewModel {

    private val _website = MutableStateFlow<Website?>(null)
    override val website: StateFlow<Website?> = _website

    private val _entry = MutableStateFlow<Entry?>(null)
    override val entry: StateFlow<Entry?> = _entry

    private var parentDir: File? = null
    override suspend fun initializeImpl() {
        _entry.value = null
        _website.value = null
        parentDir = null

        val type = UrlUtil.getWebsiteType(url).errorOnNull("Can't get website type")
        val client = betterPublicKmtt(type)
        parentDir = File("$type/entry")

        _entry.value = client.entry.getEntry(url)
        _website.value = type
    }

    override suspend fun saveImpl() {
        val parentDir = this.parentDir.errorOnNull("Parent dir is null")
        val entry = _entry.value.errorOnNull("Entry is null")

        processEntry(entry, parentDir, 0)
    }


}
