package shared.saveable

import exception.QueueElementException
import kmtt.exception.OsnovaRequestException
import kmtt.models.enums.Website
import kmtt.models.subsite.Subsite
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import shared.document.IDocumentProcessor
import shared.document.IProcessorOperation
import shared.i18n.Lang
import shared.util.kmttapi.betterPublicKmtt
import java.io.File

interface IProfileElementViewModel : ISaveable {
    val site: Website
    val user: StateFlow<Subsite?>
    val id: Long
}

class ProfileSaveable(
    override val site: Website,
    override val id: Long,
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
), IProfileElementViewModel {
    private val _user = MutableStateFlow<Subsite?>(null)
    override val user: StateFlow<Subsite?> = _user
    override suspend fun initializeImpl() {
        val client = betterPublicKmtt(site)

        try {
            _user.value = client.user.getUserByID(id)
        } catch (ex: OsnovaRequestException) {
            if (ex.httpResponse.status.value == 403) {
                throw QueueElementException(Lang.profileElementVmAccessError)
            } else {
                throw ex
            }
        }
    }

    override suspend fun saveImpl() {
        val client = betterPublicKmtt(site)
        val parentDir = baseSaveFolder.resolve("${site.name}/entry")
        var counter = 0
        val allEntriesMessage = Lang.profileElementVmAllEntriesMessage
        val errorList = mutableListOf<String>()

        setProgress(allEntriesMessage)

        client.user.getAllUserEntries(id) {
            it.forEach { entry ->
                if (!tryProcessEntry(entry, parentDir, counter)) {
                    errorList += "${site.baseURL}/${entry.id} (author=${entry.author?.name})"

                }
                counter++
            }
            setProgress(Lang.profileElementVmNextChunk)
        }

        showResult(errorList, counter, parentDir.absolutePath)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProfileSaveable

        if (site != other.site) return false
        if (id != other.id) return false
        if (_user != other._user) return false
        if (user != other.user) return false

        return true
    }

    override fun hashCode(): Int {
        var result = site.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + _user.hashCode()
        result = 31 * result + user.hashCode()
        return result
    }
}