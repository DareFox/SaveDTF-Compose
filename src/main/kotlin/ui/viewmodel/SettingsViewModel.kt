package ui.viewmodel

import kmtt.models.enums.Website
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import logic.cache.buildCache
import java.util.prefs.Preferences

object SettingsViewModel {
    private val preferences = Preferences.userRoot().node("savedtf-prefs")

    private const val ERROR_MEDIA_BOOL_KEY = "replace_error_media"
    private const val RETRY_AMOUNT_INT_KEY = "retry_amount"
    private const val SAVE_FOLDER_STR_KEY = "save_folder"
    private const val DOWNLOAD_VIDEO_BOOL_KEY = "download_video"
    private const val DOWNLOAD_IMAGE_BOOL_KEY = "download_image"

    /**
    ———————————No Updates?———————————
    ⠀⣞⢽⢪⢣⢣⢣⢫⡺⡵⣝⡮⣗⢷⢽⢽⢽⣮⡷⡽⣜⣜⢮⢺⣜⢷⢽⢝⡽⣝
    ⠸⡸⠜⠕⠕⠁⢁⢇⢏⢽⢺⣪⡳⡝⣎⣏⢯⢞⡿⣟⣷⣳⢯⡷⣽⢽⢯⣳⣫⠇
    ⠀⠀⢀⢀⢄⢬⢪⡪⡎⣆⡈⠚⠜⠕⠇⠗⠝⢕⢯⢫⣞⣯⣿⣻⡽⣏⢗⣗⠏⠀
    ⠀⠪⡪⡪⣪⢪⢺⢸⢢⢓⢆⢤⢀⠀⠀⠀⠀⠈⢊⢞⡾⣿⡯⣏⢮⠷⠁⠀⠀
    ⠀⠀⠀⠈⠊⠆⡃⠕⢕⢇⢇⢇⢇⢇⢏⢎⢎⢆⢄⠀⢑⣽⣿⢝⠲⠉⠀⠀⠀⠀
    ⠀⠀⠀⠀⠀⡿⠂⠠⠀⡇⢇⠕⢈⣀⠀⠁⠡⠣⡣⡫⣂⣿⠯⢪⠰⠂⠀⠀⠀⠀
    ⠀⠀⠀⠀⡦⡙⡂⢀⢤⢣⠣⡈⣾⡃⠠⠄⠀⡄⢱⣌⣶⢏⢊⠂⠀⠀⠀⠀⠀⠀
    ⠀⠀⠀⠀⢝⡲⣜⡮⡏⢎⢌⢂⠙⠢⠐⢀⢘⢵⣽⣿⡿⠁⠁⠀⠀⠀⠀⠀⠀⠀
    ⠀⠀⠀⠀⠨⣺⡺⡕⡕⡱⡑⡆⡕⡅⡕⡜⡼⢽⡻⠏⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
    ⠀⠀⠀⠀⣼⣳⣫⣾⣵⣗⡵⡱⡡⢣⢑⢕⢜⢕⡝⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
    ⠀⠀⠀⣴⣿⣾⣿⣿⣿⡿⡽⡑⢌⠪⡢⡣⣣⡟⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
    ⠀⠀⠀⡟⡾⣿⢿⢿⢵⣽⣾⣼⣘⢸⢸⣞⡟⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
    ⠀⠀⠀⠀⠁⠇⠡⠩⡫⢿⣝⡻⡮⣒⢽⠋⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
    ——————————————————————————————————
     */
    private const val MUTE_UPDATES_NOTIFICATION_KEY = "no_updates_${AppViewModel.VERSION}"

    private val _replaceErrorMedia = MutableStateFlow(getPrefReplaceErrorMedia())
    private val _tokens = MutableStateFlow(
        mapOf<Website, String>(
            Website.DTF to getPrefToken(Website.DTF),
            Website.VC to getPrefToken(Website.VC),
            Website.TJ to getPrefToken(Website.TJ)
        )
    )
    private val _retryAmount = MutableStateFlow(getPrefRetryAmount())
    private val _folderToSave = MutableStateFlow<String?>(getPrefFolder())
    private val _downloadVideo = MutableStateFlow(getPrefDownloadVideo())
    private val _downloadImage = MutableStateFlow(getPrefDownloadImage())
    private val _ignoreUpdate = MutableStateFlow(getPrefIgnoreUpdates())

    val replaceErrorMedia: StateFlow<Boolean> = _replaceErrorMedia
    val tokens: StateFlow<Map<Website, String>> = _tokens
    val retryAmount: StateFlow<Int> = _retryAmount;
    val folderToSave: StateFlow<String?> = _folderToSave
    val downloadVideo: StateFlow<Boolean> = _downloadVideo
    val downloadImage: StateFlow<Boolean> = _downloadImage
    val ignoreUpdate: StateFlow<Boolean> = _ignoreUpdate

    private fun getPrefReplaceErrorMedia() = preferences.getBoolean(ERROR_MEDIA_BOOL_KEY, true)
    private fun getPrefFolder() = preferences.get(SAVE_FOLDER_STR_KEY, null)
    private fun getPrefRetryAmount() = preferences.getInt(RETRY_AMOUNT_INT_KEY, 5)
    private fun getPrefToken(website: Website): String {
        return preferences.node("tkn").get(website.name, "")
    }

    private fun getPrefDownloadVideo() = preferences.getBoolean(DOWNLOAD_VIDEO_BOOL_KEY, true)
    private fun getPrefDownloadImage() = preferences.getBoolean(DOWNLOAD_IMAGE_BOOL_KEY, true)
    private fun getPrefIgnoreUpdates() = preferences.getBoolean(MUTE_UPDATES_NOTIFICATION_KEY, false)

    fun setToken(token: String?, website: Website) {
        if (token == null || token.isEmpty() || token.isBlank()) {
            preferences.node("tkn").remove(website.name)
        } else {
            preferences.node("tkn").put(website.name, token.trim())
        }

        _tokens.update {
            val map = it.toMutableMap()
            map[website] = getPrefToken(website)
            map
        }
    }

    fun setFolderToSave(folder: String?) {
        preferences.put(SAVE_FOLDER_STR_KEY, folder)
        _folderToSave.value = getPrefFolder()
    }

    fun setReplaceErrorMedia(enable: Boolean) {
        preferences.putBoolean(ERROR_MEDIA_BOOL_KEY, enable)
        _replaceErrorMedia.value = getPrefReplaceErrorMedia()
    }

    fun setRetryAmount(value: Int) {
        preferences.putInt(RETRY_AMOUNT_INT_KEY, value)
        _retryAmount.value = getPrefRetryAmount()
    }

    fun setDownloadVideoMode(downloadIt: Boolean) {
        preferences.putBoolean(DOWNLOAD_VIDEO_BOOL_KEY, downloadIt)
        _downloadVideo.value = getPrefDownloadVideo()
    }

    fun setDownloadImageMode(downloadIt: Boolean) {
        preferences.putBoolean(DOWNLOAD_IMAGE_BOOL_KEY, downloadIt)
        _downloadImage.value = getPrefDownloadImage()
    }

    fun setIgnoreUpdate(ignore: Boolean) {
        preferences.putBoolean(MUTE_UPDATES_NOTIFICATION_KEY, ignore)
        _ignoreUpdate.value = getPrefIgnoreUpdates()
    }

    fun clearCache(): Boolean {
        return buildCache().clearAll()
    }

}