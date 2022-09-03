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
import mu.KotlinLogging
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
    private val logger = KotlinLogging.logger { }
    private val parentDir: File
        get() = File(pathToSave, "${site.name}/entry")

    override suspend fun initialize() {
        elementMutex.withLock {
            initializing()
            client = betterPublicKmtt(site) // recreate client if token changed
            try {
                progress(Lang.value.profileElementVmRequestingProfile)
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
            elementMutex.withLock {
                inUse()

                var counter = 0
                val allEntriesMessage = Lang.value.profileElementVmAllEntriesMessage
                val errorList = mutableListOf<String>()
                withProgressSuspend(allEntriesMessage) {
                    client.user.getAllUserEntries(id) {
                        it.forEach { entry ->
                            if(!tryProcessDocument(entry, parentDir, counter, logger)) {
                                errorList += "${site.baseURL}/${entry.id} (author=${entry.author?.name})"

                            }
                            counter++
                        }
                        progress(Lang.value.profileElementVmNextChunk)
                    }
                }

                val errorCounter = errorList.count()

                if (errorCounter > 0) {
                    saved()
                    logger.error("Failed to download:\n${errorList.joinToString(",\n")}")
                    progress(Lang.value.profileElementVmSomeErrors.format(counter, errorCounter))
                } else if (errorCounter == counter) {
                    logger.error("Failed to download:\n${errorList.joinToString(",\n")}")
                    error(Lang.value.profileElementVmAllErrors.format(errorCounter))
                    clearProgress()
                } else {
                    saved()
                    progress(Lang.value.profileElementVmNoErrors.format(counter))
                }

                errorCounter == 0

            }
        }
    }
}