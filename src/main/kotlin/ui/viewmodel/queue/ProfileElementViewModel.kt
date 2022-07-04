package ui.viewmodel.queue

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import exception.errorOnNull
import kmtt.models.entry.Entry
import kmtt.models.enums.Website
import kmtt.models.subsite.Subsite
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.withLock
import logic.document.SettingsBasedDocumentProcessor
import org.jsoup.Jsoup
import util.coroutine.cancelOnSuspendEnd
import util.filesystem.toDirectory
import util.kmttapi.betterPublicKmtt
import util.progress.redirectTo
import java.io.File

interface IProfileElementViewModel : IQueueElementViewModel {
    val site: Website
    val user: StateFlow<Subsite?>
}

data class ProfileElementViewModel(
    override val site: Website,
    val id: Long
): AbstractElementViewModel(), IProfileElementViewModel {
    val _user = MutableStateFlow<Subsite?>(null)

    private var client = betterPublicKmtt(site)
    override val user: StateFlow<Subsite?> = _user

    private val scope = CoroutineScope(Dispatchers.Default)
    private var counter = 0
    private var errorCounter = 0

    override suspend fun initialize() {
        elementMutex.withLock {
            initializing()
            try {
                _user.value = client.user.getUserByID(id)

                readyToUse()
            } catch (ex: Exception) {
                error("Can't get user profile. Exception: $ex")
            }
        }
    }

    override suspend fun save(): Boolean {
        var result = true
        val allEntriesMessage = "Getting all entries..." +
                " If you have a lot of entries, it could take a long time to get all of them"

        throw Exception("sus")
        elementMutex.withLock {
            inUse()
            withProgressSuspend(allEntriesMessage) {
                client.user.getAllUserEntries(id) {
                    if (!processDocument(it)) { // process document and if there is error, change final result to false
                        result = false
                    }
                    progress("Requesting next chunk of entries...")
                }
            }

            if (errorCounter > 0) {
                saved()
                progress("Downloaded $counter bookmarks. Couldn't download $errorCounter bookmarks")
            } else if (errorCounter == counter) {
                error("Couldn't download any of $errorCounter bookmarks")
                clearProgress()
            } else {
                saved()
                progress("Downloaded all $counter bookmarks.")
            }
        }

        counter = 0
        return result
    }

    private suspend fun processDocument(list: List<Entry>): Boolean {
        var result = true

        for (entry in list) {
            try {
                val document = entry
                    .entryContent
                    .errorOnNull("Entry content is null")
                    .html
                    .errorOnNull("Entry html is null")
                    .let { Jsoup.parse(it) } // parse document

                val processor = SettingsBasedDocumentProcessor(entry.toDirectory(File(pathToSave)), document)
                val newCounter = ++counter

                processor
                    .redirectTo(mutableProgress, scope) {// redirect progress of processor to this VM progress
                        "Entry #${newCounter}, $it" // show entry counter
                    }
                    .cancelOnSuspendEnd {
                        processor.process() // save document
                    }

            } catch (ex: Exception) { // on error, change result to false
                result = false
                errorCounter++
            }
        }

        return result
    }
}