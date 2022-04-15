package ui.viewmodel

import kmtt.models.enums.Website
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.util.prefs.Preferences

object SettingsViewModel {
    private val preferences = Preferences.userRoot().node("savedtf-prefs")

    private val _folderToSave = MutableStateFlow<String>(getPrefFolder())
    private fun getPrefFolder() = preferences.get("save_folder", File("./saved").canonicalPath)
    val folderToSave: StateFlow<String?> = _folderToSave

    private fun getToken(website: Website) = preferences.node("tkn").get(website.name, null)
    private val _tokens = MutableStateFlow(mapOf<Website, String?>(
        Website.DTF to getToken(Website.DTF),
        Website.VC to getToken(Website.VC),
        Website.TJ to getToken(Website.TJ)
    ))
    val tokens: StateFlow<Map<Website, String?>> = _tokens

    fun setToken(token: String, website: Website) {
        preferences.node("tkn").put(website.name, token)

        _tokens.update {
            val map = it.toMutableMap()
            map[website] = getToken(website)
            map
        }
    }

    fun setFolderToSave(folder: String) {
        preferences.put("save_folder", folder)
        _folderToSave.value = getPrefFolder()
    }
}