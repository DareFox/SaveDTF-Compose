package ui.viewmodel.queue

import exception.errorOnNull
import kmtt.impl.authKmtt
import kmtt.models.entry.Entry
import kmtt.models.enums.Website
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.withLock
import logic.document.SettingsBasedDocumentProcessor
import org.jsoup.Jsoup
import ui.i18n.Lang
import ui.viewmodel.SettingsViewModel
import ui.viewmodel.SettingsViewModel.getToken
import util.coroutine.cancelOnSuspendEnd
import util.filesystem.toDirectory
import util.progress.redirectTo
import java.io.File

interface IBookmarksElementViewModel : IQueueElementViewModel {
    val site: Website
}

data class BookmarksElementViewModel(
    override val site: Website,
) : AbstractElementViewModel(), IBookmarksElementViewModel {
    private val token: String
        get() = SettingsViewModel.tokens.getToken(site)
    private var client = authKmtt(site, token)
    private var counter = 0
    private var errorCounter = 0

    override suspend fun initialize() {
        elementMutex.withLock {
            initializing()
            // if token updates we should recreate api client
            client = authKmtt(site, token)
            try {
                // check if token works
                client.user.getMe()
                readyToUse()
            } catch (ex: Exception) {
                error(Lang.value.bookmarksElementVmProfileError)
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

                val processor = SettingsBasedDocumentProcessor(entry.toDirectory(File(pathToSave, "bookmarks/${site.name}")), document, entry)
                val newCounter = ++counter

                processor
                    .redirectTo(mutableProgress, ioScope) {// redirect progress of processor to this VM progress
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

    override suspend fun save(): Deferred<Boolean> {
        return waitAndAsyncJob {
            var result = true

            elementMutex.withLock { // run only 1 function at a time
                inUse()
                withProgressSuspend(Lang.value.bookmarksElementVmAllEntriesMessage) { // show progress message at start
                    client.user.getAllMyFavoriteEntries { // save each chunk
                        if (!processDocument(it)) { // process document and if there is error, change final result to false
                            result = false
                        }
                        progress(Lang.value.bookmarksElementVmNextChunk)
                    }
                }

                if (errorCounter > 0) {
                    saved()
                    progress(Lang.value.bookmarksElementVmSomeErrors.format(counter, errorCounter))
                } else if (errorCounter == counter) {
                    error(Lang.value.bookmarksElementVmAllErrors.format(errorCounter))
                    clearProgress()
                } else {
                    saved()
                    progress(Lang.value.bookmarksElementVmNoErrors.format(counter))
                }
            }

            counter = 0
            result
        }
    }
}