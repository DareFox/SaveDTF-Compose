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
                client.user.getMe()
                readyToUse()
            } catch (ex: Exception) {
                error(Lang.value.bookmarksElementVmProfileError)
            }
        }
    }
    override suspend fun save(): Deferred<Boolean> {
        return waitAndAsyncJob {
            var errorCounter = 0
            var counter = 0

            elementMutex.withLock { // run only 1 function at a time
                inUse()
                withProgressSuspend(Lang.value.bookmarksElementVmAllEntriesMessage) { // show progress message at start
                    client.user.getAllMyFavoriteEntries {
                        it.forEach { entry ->
                            if (!tryProcessDocument(entry, parentDir, counter)) {
                                errorCounter++
                            }
                            counter++
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

            errorCounter > 0
        }
    }
}