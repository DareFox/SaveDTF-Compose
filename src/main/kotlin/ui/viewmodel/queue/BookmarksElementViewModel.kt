package ui.viewmodel.queue

import exception.errorOnNull
import kmtt.impl.authKmtt
import kmtt.models.entry.Entry
import kmtt.models.enums.Website
import kmtt.models.subsite.Subsite
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.withLock
import logic.document.SettingsBasedDocumentProcessor
import org.jsoup.Jsoup
import util.coroutine.cancelOnSuspendEnd
import util.filesystem.toDirectory
import util.progress.redirectTo
import java.io.File

interface IBookmarksElementViewModel : IQueueElementViewModel {
    val site: Website
    val token: String
}

class BookmarksElementViewModel(
    override val site: Website,
    override val token: String,
) : AbstractElementViewModel(), IBookmarksElementViewModel {
    private val client = authKmtt(site, token)
    private val scope = CoroutineScope(Dispatchers.Default)
    private var counter = 0
    private var errorCounter = 0

    override suspend fun initialize() {
        mutexInitializer.withLock {
            initializing()
            try {
                // check if token works
                client.user.getMe()
                readyToUse()
            } catch (ex: Exception) {
                error("Error while requesting your profile. Is your token in settings correct?")
            }
        }
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

    override suspend fun save(): Boolean {
        var result = true
        val allEntriesMessage = "Getting all entries..." +
                " If you have a lot of entries, it could take a long time to get all of them"

        mutexInitializer.withLock { // run only 1 function at a time
            withProgressSuspend(allEntriesMessage) { // show progress message at start
                client.user.getAllMyFavoriteEntries { // save each chunk
                    if (!processDocument(it)) { // if there is error, change result to false
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

}