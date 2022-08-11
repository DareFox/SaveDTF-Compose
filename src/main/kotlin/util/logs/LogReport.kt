package util

import kmtt.models.enums.Website
import me.darefox.saveDTF_compose.BuildConfig
import mu.KotlinLogging
import ui.viewmodel.SettingsViewModel
import util.logs.getCurrentLogFile

private val logger = KotlinLogging.logger { }
fun getCrashLogReport(ex: Throwable): String {
    val stringBuilder = StringBuilder(10000)

    stringBuilder.title("PROGRAM VERSION")
    stringBuilder.append(
        """
        SaveDTF_Version = ${BuildConfig.APP_FULL_VERSION}
        SaveDTF_SemanticVersion = ${BuildConfig.APP_SEMANTIC_VERSION}
        SaveDTF_BuildNumber = ${BuildConfig.APP_BUILD_NUMBER}
        SaveDTF_Is_Dev_Version = ${BuildConfig.IS_DEV_VERSION}
    """.trimIndent()
    )

    stringBuilder.title("PROGRAM SETTINGS")
    stringBuilder.append(
        """
        Replace error media = ${SettingsViewModel.replaceErrorMedia.value} 
        Retry amount = ${SettingsViewModel.retryAmount.value} 
        Media timeout in seconds = ${SettingsViewModel.mediaTimeoutInSeconds.value} 
        Entry timeout in seconds = ${SettingsViewModel.entryTimeoutInSeconds.value} 
        Save folder is set? = ${!SettingsViewModel.folderToSave.value.isNullOrEmpty()} 
        Download video = ${SettingsViewModel.downloadVideo.value} 
        Download image = ${SettingsViewModel.downloadImage.value} 
        Ignore updates = ${SettingsViewModel.ignoreUpdate.value} 
        Language = ${SettingsViewModel.proxyLocale.value.localeName} (${SettingsViewModel.proxyLocale.value.localeTag}) 
    
        Tokens:
    """.trimIndent()
    )

    // DON'T SHOW TOKEN VALUES IN LOGS!!!
    val tokens = Website.values().joinToString(separator = "") {
        "\n\t$it - ${!SettingsViewModel.tokens.value[it].isNullOrBlank()}"
    }
    stringBuilder.append(tokens)

    stringBuilder.title("SYSTEM INFO")
    stringBuilder.append(
        """
        os.name = ${System.getProperty("os.name")}
        java.version = ${System.getProperty("java.version")}
    """.trimIndent()
    )

    stringBuilder.title("LOGS")
    val log = getCurrentLogFile()
    if (log == null) {
        stringBuilder.append("Can't find log file")
    } else {
        val level = SettingsViewModel.loggerLevel.value
        val linesNum = if (level != SettingsViewModel.LoggerLevel.DEBUG) {
            200
        } else {
            500
        }
        stringBuilder.append("Level: ${level.name}")
        stringBuilder.append("Last $linesNum lines\n")
        log.readLines().takeLast(linesNum).forEach {
            stringBuilder.append(it + "\n")
        }
    }


    stringBuilder.title("STACKTRACE")
    stringBuilder.append(ex.stackTraceToString())

    return stringBuilder.toString().trim()
}

private fun StringBuilder.title(title: String) {
    this.append("\n\n========= $title =========\n")
}