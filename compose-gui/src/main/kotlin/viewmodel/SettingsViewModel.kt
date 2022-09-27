package viewmodel

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import kmtt.models.enums.Website
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import logic.cache.buildCache
import org.slf4j.LoggerFactory
import ui.i18n.AvailableLanguages
import ui.i18n.DefaultLanguageResource
import ui.i18n.LanguageResource
import ui.i18n.ProxyLanguageResource
import java.util.prefs.Preferences

object SettingsViewModel {
    private val preferences = Preferences.userRoot().node("savedtf-prefs")
    private val eventListeners = mutableListOf<() -> Unit>()

    enum class LoggerLevel {
        INFO, DEBUG
    }

    private const val ERROR_MEDIA_BOOL_KEY = "replace_error_media"
    private const val MEDIA_RETRY_AMOUNT_KEY = "retry_amount"
    private const val MEDIA_TIMEOUT_TIME_KEY = "timeout_time_media"
    private const val ENTRY_TIMEOUT_TIME_KEY = "timeout_time_entry"
    private const val SAVE_FOLDER_STR_KEY = "save_folder"
    private const val DOWNLOAD_VIDEO_BOOL_KEY = "download_video"
    private const val DOWNLOAD_IMAGE_BOOL_KEY = "download_image"
    private const val PROGRAM_LOCALE_KEY = "program_locale"
    private const val LOGGER_LEVEL = "logger_level"
    private const val SAVE_METADATA_KEY = "save_metadata"
    private const val SAVE_COMMENTS_KEY = "save_comments"
    private const val API_TIMEOUT_TIME_KEY = "timeout_time_api"

    /**
    ———————————No Updates?——————————————
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
    —————————————————————————————————
     */
    private const val MUTE_UPDATES_NOTIFICATION_KEY = "no_updates_${AppViewModel.VERSION}"

    private val _replaceErrorMedia = createUpdatableState { getPrefReplaceErrorMedia() }
    private val _tokens = createUpdatableState { getPrefAllTokens() }

    /**
     * Get token from current state.
     */
    fun StateFlow<Map<Website, String>>.getToken(website: Website): String {
        return this.value.get(website)!!
    }

    private val _retryAmount = createUpdatableState { getPrefRetryAmount() }
    private val _mediaTimeoutInSeconds = createUpdatableState { getPrefMediaTimeout() }
    private val _entryTimeoutInSeconds = createUpdatableState { getPrefEntryTimeout() }
    private val _apiTimeoutInSeconds = createUpdatableState { getPrefApiTimeout() }
    private val _folderToSave = createUpdatableState<String?> { getPrefFolder() }
    private val _downloadVideo = createUpdatableState { getPrefDownloadVideo() }
    private val _downloadImage = createUpdatableState { getPrefDownloadImage() }
    private val _ignoreUpdate = createUpdatableState { getPrefIgnoreUpdates() }
    private val _proxyLocale = createUpdatableState { getProxyLocale() }
    private val _loggerLevel = createUpdatableState { getLoggerLevel() }
    private val _saveMetadta = createUpdatableState { getSaveMetadata() }
    private val _saveComments = createUpdatableState { getSaveComments() }

    val replaceErrorMedia: StateFlow<Boolean> = _replaceErrorMedia
    val tokens: StateFlow<Map<Website, String>> = _tokens
    val retryAmount: StateFlow<Int> = _retryAmount
    val mediaTimeoutInSeconds: StateFlow<Int> = _mediaTimeoutInSeconds
    val entryTimeoutInSeconds: StateFlow<Int> = _entryTimeoutInSeconds
    val apiTimeoutInSeconds: StateFlow<Int> = _apiTimeoutInSeconds
    val folderToSave: StateFlow<String?> = _folderToSave
    val downloadVideo: StateFlow<Boolean> = _downloadVideo
    val downloadImage: StateFlow<Boolean> = _downloadImage
    val ignoreUpdate: StateFlow<Boolean> = _ignoreUpdate
    val proxyLocale: StateFlow<LanguageResource> = _proxyLocale
    val loggerLevel: StateFlow<LoggerLevel> = _loggerLevel
    val saveMetadata: StateFlow<Boolean> = _saveMetadta
    val saveComments: StateFlow<Boolean> = _saveComments

    private fun getPrefAllTokens() = mapOf(
        Website.DTF to getPrefToken(Website.DTF),
        Website.VC to getPrefToken(Website.VC),
        Website.TJ to getPrefToken(Website.TJ)
    )

    private fun getPrefReplaceErrorMedia() = preferences.getBoolean(ERROR_MEDIA_BOOL_KEY, true)
    private fun getPrefFolder() = preferences.get(SAVE_FOLDER_STR_KEY, null)
    private fun getPrefRetryAmount() = preferences.getInt(MEDIA_RETRY_AMOUNT_KEY, 3)
    private fun getPrefToken(website: Website): String {
        return preferences.node("tkn").get(website.name, "")
    }

    private fun getPrefDownloadVideo() = preferences.getBoolean(DOWNLOAD_VIDEO_BOOL_KEY, true)
    private fun getSaveMetadata() = preferences.getBoolean(SAVE_METADATA_KEY, true)
    private fun getSaveComments() = preferences.getBoolean(SAVE_COMMENTS_KEY, true)
    private fun getPrefDownloadImage() = preferences.getBoolean(DOWNLOAD_IMAGE_BOOL_KEY, true)
    private fun getPrefIgnoreUpdates() = preferences.getBoolean(MUTE_UPDATES_NOTIFICATION_KEY, false)
    private fun getPrefMediaTimeout() = preferences.getInt(MEDIA_TIMEOUT_TIME_KEY, 300)
    private fun getPrefApiTimeout() = preferences.getInt(API_TIMEOUT_TIME_KEY, 90)
    private fun getPrefEntryTimeout() = preferences.getInt(ENTRY_TIMEOUT_TIME_KEY, -1)
    private fun getPrefLocalTag() = preferences.get(PROGRAM_LOCALE_KEY, DefaultLanguageResource.localeName)
    private fun getProxyLocale(default: LanguageResource = DefaultLanguageResource): LanguageResource {
        val tag = getPrefLocalTag()
        return ProxyLanguageResource(AvailableLanguages.firstOrNull { tag == it.localeTag } ?: default, default)
    }

    private fun getLoggerLevel(): LoggerLevel {
        val level = preferences.get(LOGGER_LEVEL, "DEBUG")

        val enum = LoggerLevel.values().first {
            it.name.uppercase() == level.uppercase()
        }

        setLogbackLevel(enum)
        return enum
    }

    fun setSaveComments(enable: Boolean) {
        preferences.putBoolean(SAVE_COMMENTS_KEY, enable)
        _saveComments.value = getSaveComments()
    }

    fun setApiTimeoutInSeconds(timeout: Int) {
        preferences.putInt(API_TIMEOUT_TIME_KEY, timeout)
        _apiTimeoutInSeconds.value = getPrefApiTimeout()
    }

    fun setToken(token: String?, website: Website) {
        if (token == null || token.isEmpty() || token.isBlank()) {
            preferences.node("tkn").remove(website.name)
        } else {
            preferences.node("tkn").put(website.name, token.trim())
        }

        _tokens.update { getPrefAllTokens() }
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
        preferences.putInt(MEDIA_RETRY_AMOUNT_KEY, value)
        _retryAmount.value = getPrefRetryAmount()
    }

    fun setMediaTimeoutInSeconds(value: Int) {
        preferences.putInt(MEDIA_TIMEOUT_TIME_KEY, value)
        _mediaTimeoutInSeconds.value = getPrefMediaTimeout()
    }

    fun setEntryTimeoutInSeconds(value: Int) {
        preferences.putInt(ENTRY_TIMEOUT_TIME_KEY, value)
        _entryTimeoutInSeconds.value = getPrefEntryTimeout()
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

    fun setLocale(localeTag: String) {
        preferences.put(PROGRAM_LOCALE_KEY, localeTag)
        _proxyLocale.value = getProxyLocale()
    }

    fun setLocale(language: LanguageResource) {
        preferences.put(PROGRAM_LOCALE_KEY, language.localeTag)
        _proxyLocale.value = getProxyLocale()
    }

    fun setLoggerLevel(level: LoggerLevel) {
        preferences.put(LOGGER_LEVEL, level.name)
        _loggerLevel.value = getLoggerLevel()
    }

    fun setSaveMetadataMode(shouldDownload: Boolean) {
        preferences.putBoolean(SAVE_METADATA_KEY, shouldDownload)
        _saveMetadta.value = getSaveMetadata()
    }

    fun clearCache(): Boolean {
        return buildCache().clearAll()
    }

    /**
     * Reset all preferences.
     *
     * True on success, false on failure
     */
    fun resetAllSettings(): Boolean {
        return (try {
            recursivePreferencesReset(preferences)
            true
        } catch (ex: Exception) {
            false
        }).also { updateAllStates() }
    }

    /**
     * Create MutableStateFlow that updates on [updateAllStates] funciton call
     */
    private fun <T> createUpdatableState(getter: () -> T): MutableStateFlow<T> {
        val state = MutableStateFlow(getter())

        eventListeners.add {
            state.update { getter() }
        }

        return state
    }

    /**
     * Update all states by calling set up getter function in [eventListeners]
     */
    private fun updateAllStates() {
        eventListeners.forEach {
            it()
        }
    }

    private fun recursivePreferencesReset(preferences: Preferences) {
        // Remove all key-value pairs (child nodes not affected)
        preferences.clear()

        // Retrieve all child nodes of this preferences object and clear them too
        preferences.childrenNames().forEach {
            recursivePreferencesReset(preferences.node(it))
        }
    }

    private fun setLogbackLevel(enum: LoggerLevel) {
        val toSet = when (enum) {
            LoggerLevel.INFO -> Level.INFO
            LoggerLevel.DEBUG -> Level.DEBUG
        }

        val logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger

        logger.level = toSet
    }
}