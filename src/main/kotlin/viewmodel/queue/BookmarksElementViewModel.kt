package viewmodel.queue

import exception.QueueElementException
import kmtt.impl.authKmtt
import kmtt.models.enums.Website
import kotlinx.coroutines.sync.withLock
import ui.i18n.Lang
import viewmodel.SettingsViewModel
import viewmodel.SettingsViewModel.getToken
import java.io.File

interface IBookmarksElementViewModel : IQueueElementViewModel {
    val site: Website
}

data class BookmarksElementViewModel(
    override val site: Website,
) : AbstractElementViewModel({}), IBookmarksElementViewModel {
    private val token: String
        get() = SettingsViewModel.tokens.getToken(site)
    private var client = authKmtt(site, token)
    override suspend fun initializeImpl() {
        // if token updates we should recreate api client
        client = authKmtt(site, token)
        setProgress(Lang.value.bookmarksElementVmRequestingProfile)

        try {
            client.user.getMe()
        } catch (e: Exception) {
            throw QueueElementException(Lang.value.bookmarksElementVmProfileError)
        }
    }

    override suspend fun saveImpl() {
        var counter = 0
        val parentDirFile = File(baseSaveFolder, "${site.name}/bookmarks")
        val errorList = mutableListOf<String>()

        setProgress(Lang.value.bookmarksElementVmAllEntriesMessage)
        client.user.getAllMyFavoriteEntries {
            it.forEach { entry ->
                if (!tryProcessEntry(entry, parentDirFile, counter)) {
                    errorList += "${site.baseURL}/${entry.id} (author=${entry.author?.name})"
                }
                counter++
            }
            setProgress(Lang.value.bookmarksElementVmNextChunk)
        }

        showResult(errorList, counter, parentDirFile.absolutePath)
    }
}