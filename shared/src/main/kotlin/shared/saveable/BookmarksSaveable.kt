package shared.saveable

import exception.QueueElementException
import kmtt.impl.authKmtt
import kmtt.models.enums.Website
import shared.document.IProcessorOperation
import shared.i18n.Lang
import java.io.File

interface IBookmarksSaveable : ISaveable {
    val site: Website
}

class BookmarksSaveable(
    override val site: Website,
    private val token: String,
    apiTimeoutInSeconds: Int,
    entryTimeoutInSeconds: Int,
    operations: Set<IProcessorOperation>,
    folderToSave: File,
) : AbstractSaveable(
    emptyLambda = {},
    apiTimeoutInSeconds = apiTimeoutInSeconds,
    entryTimeoutInSeconds = entryTimeoutInSeconds,
    operations = operations,
    folderToSave = folderToSave
), IBookmarksSaveable {
    private var client = authKmtt(site, token)
    override suspend fun initializeImpl() {
        // if token updates we should recreate api client
        client = authKmtt(site, token)
        setProgress(Lang.bookmarksElementVmRequestingProfile)

        try {
            client.user.getMe()
        } catch (e: Exception) {
            throw QueueElementException(Lang.bookmarksElementVmProfileError)
        }
    }

    override suspend fun saveImpl() {
        var counter = 0
        val parentDirFile = baseSaveFolder.resolve("${site.name}/bookmarks")
        val errorList = mutableListOf<String>()

        setProgress(Lang.bookmarksElementVmAllEntriesMessage)
        client.user.getAllMyFavoriteEntries {
            it.forEach { entry ->
                if (!tryProcessEntry(entry, parentDirFile, counter)) {
                    errorList += "${site.baseURL}/${entry.id} (author=${entry.author?.name})"
                }
                counter++
            }
            setProgress(Lang.bookmarksElementVmNextChunk)
        }

        showResult(errorList, counter, parentDirFile.absolutePath)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BookmarksSaveable

        if (site != other.site) return false
        if (token != other.token) return false

        return true
    }

    override fun hashCode(): Int {
        var result = site.hashCode()
        result = 31 * result + token.hashCode()
        return result
    }
}