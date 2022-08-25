package viewmodel.queue

import exception.errorOnNull
import kmtt.exception.OsnovaRequestException
import kmtt.models.entry.Entry
import kmtt.models.enums.Website
import kmtt.models.subsite.Subsite
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.yield
import logic.document.SettingsBasedDocumentProcessor
import org.jsoup.Jsoup
import ui.i18n.Lang
import util.coroutine.cancelOnSuspendEnd
import util.filesystem.toDirectory
import util.kmttapi.betterPublicKmtt
import util.progress.redirectTo
import java.io.File

interface IProfileElementViewModel : IQueueElementViewModel {
    val site: Website
    val user: StateFlow<Subsite?>
    val id: Long
}

data class ProfileElementViewModel(
    override val site: Website,
    override val id: Long
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
            client = betterPublicKmtt(site) // recreate client if token changed
            try {
                _user.value = client.user.getUserByID(id)
                readyToUse()
            } catch(ex: OsnovaRequestException) {
                // 403 Forbidden
                if (ex.httpResponse.status.value == 403) {
                    error(Lang.value.profileElementVmAccessError)
                } else {
                    error(Lang.value.profileElementVmGenericInitError.format(ex))
                }
            } catch (ex: Exception) {
                error(Lang.value.profileElementVmGenericInitError.format(ex))
            }
        }
    }

    override suspend fun save(): Deferred<Boolean> {
        return waitAndAsyncJob {
            var result = true
            val allEntriesMessage = Lang.value.profileElementVmAllEntriesMessage

            elementMutex.withLock {
                inUse()
                withProgressSuspend(allEntriesMessage) {
                    client.user.getAllUserEntries(id) {
                        yield()
                        if (!processDocument(it)) { // process document and if there is error, change final result to false
                            result = false
                        }
                        progress(Lang.value.profileElementVmNextChunk)
                    }
                }

                if (errorCounter > 0) {
                    saved()
                    progress(Lang.value.profileElementVmSomeErrors.format(counter, errorCounter))
                } else if (errorCounter == counter) {
                    error(Lang.value.profileElementVmAllErrors.format(errorCounter))
                    clearProgress()
                } else {
                    saved()
                    progress(Lang.value.profileElementVmNoErrors.format(counter))
                }
            }

            counter = 0
            result
        }
    }

    // TODO: Remove code duplication
    private suspend fun processDocument(list: List<Entry>): Boolean {
        var result = true

        for (entry in list) {
            yield()
            try {
                val document = entry
                    .entryContent
                    .errorOnNull("Entry content is null")
                    .html
                    .errorOnNull("Entry html is null")
                    .let { Jsoup.parse(it) } // parse document

                val processor = SettingsBasedDocumentProcessor(entry.toDirectory(File(pathToSave)), document, entry)
                val newCounter = ++counter

                processor
                    .redirectTo(mutableProgress, scope) {// redirect progress of processor to this VM progress
                        val progressValue = it?.run { ", $this" } ?: ""

                        // show entry counter
                        if (currentJob.value?.isCancelled != true) "${Lang.value.queueVmEntry} #${newCounter}$progressValue"
                        // show nothing on cancellation
                        else null
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