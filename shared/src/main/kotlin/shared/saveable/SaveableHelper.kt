package shared.saveable

import kmtt.models.enums.Website
import shared.document.IProcessorOperation
import shared.ktor.HttpClient
import java.io.File

/**
 * Class for reducing boilerplate arguments code for creating [ISaveable] objects
 *
 * @see allEntries
 * @see bookmarks
 * @see entry
 * @see periodEntries
 * @see profile
 */
class SaveableHelper(
    var apiTimeoutInSeconds: Int,
    var entryTimeoutInSeconds: Int,
    operations: Set<IProcessorOperation>,
    var folderToSave: File,
    val httpClient: HttpClient
) {
    /**
     * Operations to be applied when creating [ISaveable] objects
     */
    var operations: MutableSet<IProcessorOperation> = operations.toMutableSet();

    /**
     * Create [AllEntriesSaveable] with predefined [SaveableHelper] arguments
     */
    fun allEntries(site: Website): AllEntriesSaveable = AllEntriesSaveable(
        site = site,
        apiTimeoutInSeconds = apiTimeoutInSeconds,
        entryTimeoutInSeconds = entryTimeoutInSeconds,
        operations = operations,
        folderToSave = folderToSave,
        httpClient = httpClient
    )

    /**
     * Create [BookmarksSaveable] with predefined [SaveableHelper] arguments
     */
    fun bookmarks(site: Website, token: String) = BookmarksSaveable(
        site = site,
        token = token,
        apiTimeoutInSeconds = apiTimeoutInSeconds,
        entryTimeoutInSeconds = entryTimeoutInSeconds,
        operations = operations,
        folderToSave = folderToSave,
        httpClient = httpClient
    )

    /**
     * Create [EntrySaveable] with predefined [SaveableHelper] arguments
     */
    fun entry(url: String) = EntrySaveable(
        url = url,
        apiTimeoutInSeconds = apiTimeoutInSeconds,
        entryTimeoutInSeconds = entryTimeoutInSeconds,
        operations = operations,
        folderToSave = folderToSave,
        httpClient = httpClient
    )

    /**
     * Create [PeriodEntriesSaveable] with predefined [SaveableHelper] arguments
     */
    fun periodEntries(periodSitemapLink: String, site: Website) = PeriodEntriesSaveable(
        periodSitemapLink = periodSitemapLink,
        site = site,
        apiTimeoutInSeconds = apiTimeoutInSeconds,
        entryTimeoutInSeconds = entryTimeoutInSeconds,
        operations = operations,
        folderToSave = folderToSave,
        httpClient = httpClient
    )

    /**
     * Create [ProfileSaveable] with predefined [SaveableHelper] arguments
     */
    fun profile(site: Website, id: Long) = ProfileSaveable(
        site = site,
        id = id,
        apiTimeoutInSeconds = apiTimeoutInSeconds,
        entryTimeoutInSeconds = entryTimeoutInSeconds,
        operations = operations,
        folderToSave = folderToSave,
        httpClient = httpClient
    )
}