package viewmodel.queue

import exception.errorOnNull
import kmtt.models.entry.Entry
import kmtt.models.enums.Website
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import shared.util.kmttapi.KmttUrl
import shared.util.kmttapi.betterPublicKmtt
import java.io.File

interface IEntryQueueElementViewModel : IQueueElementViewModel {
    val url: String
    val entry: StateFlow<Entry?>
    val website: StateFlow<Website?>
}

data class EntryQueueElementViewModel(override val url: String) : AbstractElementViewModel({}),
    IEntryQueueElementViewModel {

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


}
