package viewmodel.queue

import kmtt.impl.authKmtt
import kmtt.models.enums.Website
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import ui.i18n.Lang
import viewmodel.SettingsViewModel
import viewmodel.SettingsViewModel.getToken
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
    private val logger = KotlinLogging.logger {  }
    private val parentDir = File(pathToSave, "${site.name}/bookmarks")

    override suspend fun initialize() {
        elementMutex.withLock {
            initializing()
            // if token updates we should recreate api client
            client = authKmtt(site, token)
            try {
                // check if token works
                progress(Lang.value.bookmarksElementVmRequestingProfile)
                client.user.getMe()
                clearProgress()
                readyToUse()
            } catch (ex: Exception) {
                error(Lang.value.bookmarksElementVmProfileError)
            }
        }
    }
    override suspend fun save(): Deferred<Boolean> {
        return waitAndAsyncJob {
            var counter = 0

            elementMutex.withLock { // run only 1 function at a time
                inUse()

                val errorList = mutableListOf<String>()
                withProgressSuspend(Lang.value.bookmarksElementVmAllEntriesMessage) { // show progress message at start
                    client.user.getAllMyFavoriteEntries {
                        it.forEach { entry ->
                            if (!tryProcessDocument(entry, parentDir, counter)) {
                                errorList += "${site.baseURL}/${entry.id} (author=${entry.author?.name})"
                            }
                            counter++
                        }
                        progress(Lang.value.bookmarksElementVmNextChunk)
                    }
                }

                val errorCounter = errorList.count()

                if (errorCounter > 0) {
                    saved()
                    logger.error("Failed to download:\n${errorList.joinToString(",\n")}")
                    progress(Lang.value.bookmarksElementVmSomeErrors.format(counter, errorCounter))
                } else if (errorCounter == counter) {
                    logger.error("Failed to download:\n${errorList.joinToString(",\n")}")
                    error(Lang.value.bookmarksElementVmAllErrors.format(errorCounter))
                    clearProgress()
                } else {
                    saved()
                    progress(Lang.value.bookmarksElementVmNoErrors.format(counter))
                }

                errorCounter == 0
            }

        }
    }
}