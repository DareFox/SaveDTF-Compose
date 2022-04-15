package ui.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.util.prefs.Preferences

object SettingsViewModel {
    private val preferences = Preferences.userRoot().node("savedtf-prefs")

    private val _token = MutableStateFlow<String?>(getPrefToken())
    private fun getPrefToken() = preferences.get("tkn", null)
    val token: StateFlow<String?> = _token

    private val _folderToSave = MutableStateFlow<String>(getPrefFolder())
    private fun getPrefFolder() = preferences.get("save_folder", File("./saved").canonicalPath)
    val folderToSave: StateFlow<String?> = _folderToSave

    fun setToken(token: String) {
        preferences.put("tkn", token)
        _token.value = getPrefToken()
    }

    fun setFolderToSave(folder: String) {
        preferences.put("save_folder", folder)
        _folderToSave.value = getPrefFolder()
    }
}