package shared.saveable

import exception.errorOnNull
import kmtt.models.entry.Entry
import kmtt.models.enums.Website
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import shared.document.IDocumentProcessor
import shared.document.IProcessorOperation
import shared.util.kmttapi.KmttUrl
import shared.util.kmttapi.betterPublicKmtt
import java.io.File

interface IEntrySaveable : ISaveable {
    val url: String
    val entry: StateFlow<Entry?>
    val website: StateFlow<Website?>
}

class EntrySaveable(
    override val url: String,
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
),
    IEntrySaveable {

    private val _website = MutableStateFlow<Website?>(null)
    override val website: StateFlow<Website?> = _website

    private val _entry = MutableStateFlow<Entry?>(null)
    override val entry: StateFlow<Entry?> = _entry

    private var parentDir: File? = null
    override suspend fun initializeImpl() {
        _entry.value = null
        _website.value = null
        parentDir = null

        val type = KmttUrl.getWebsiteType(url).errorOnNull("Can't get website type")
        val client = betterPublicKmtt(type)
        parentDir = File("$type/entry")

        _entry.value = client.entry.getEntry(url)
        _website.value = type
    }

    override suspend fun saveImpl() {
        val parentDir = this.parentDir.errorOnNull("Parent dir is null")
        val entry = _entry.value.errorOnNull("Entry is null")

        processEntry(entry, baseSaveFolder.resolve(parentDir), 0)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EntrySaveable

        if (url != other.url) return false
        if (_website != other._website) return false
        if (website != other.website) return false
        if (_entry != other._entry) return false
        if (entry != other.entry) return false
        if (parentDir != other.parentDir) return false

        return true
    }

    override fun hashCode(): Int {
        var result = url.hashCode()
        result = 31 * result + _website.hashCode()
        result = 31 * result + website.hashCode()
        result = 31 * result + _entry.hashCode()
        result = 31 * result + entry.hashCode()
        result = 31 * result + (parentDir?.hashCode() ?: 0)
        return result
    }


}
