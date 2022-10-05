package viewmodel.queue

import exception.QueueElementException
import kmtt.exception.OsnovaRequestException
import kmtt.models.enums.Website
import kmtt.models.subsite.Subsite
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ui.i18n.Lang
import shared.util.kmttapi.betterPublicKmtt

interface IProfileElementViewModel : IQueueElementViewModel {
    val site: Website
    val user: StateFlow<Subsite?>
    val id: Long
}

data class ProfileElementViewModel(
    override val site: Website,
    override val id: Long
) : AbstractElementViewModel({}), IProfileElementViewModel {
    private val _user = MutableStateFlow<Subsite?>(null)
    override val user: StateFlow<Subsite?> = _user
    override suspend fun initializeImpl() {
        val client = betterPublicKmtt(site)

        try {
            _user.value = client.user.getUserByID(id)
        } catch (ex: OsnovaRequestException) {
            if (ex.httpResponse.status.value == 403) {
                throw QueueElementException(Lang.value.profileElementVmAccessError)
            } else {
                throw ex
            }
        }
    }

    override suspend fun saveImpl() {
        val client = betterPublicKmtt(site)
        val parentDir = baseSaveFolder.resolve("${site.name}/entry")
        var counter = 0
        val allEntriesMessage = Lang.value.profileElementVmAllEntriesMessage
        val errorList = mutableListOf<String>()

        setProgress(allEntriesMessage)

        client.user.getAllUserEntries(id) {
            it.forEach { entry ->
                if (!tryProcessEntry(entry, parentDir, counter)) {
                    errorList += "${site.baseURL}/${entry.id} (author=${entry.author?.name})"

                }
                counter++
            }
            setProgress(Lang.value.profileElementVmNextChunk)
        }

        showResult(errorList, counter, parentDir.absolutePath)
    }


}