import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import mu.KotlinLogging
import shared.document.AbstractProcessorOperation
import shared.document.IProcessorOperation
import shared.document.operations.*
import shared.document.operations.format.FormatHtmlOperation
import shared.document.operations.media.SaveMediaOperation
import shared.document.operations.media.modules.IDownloadModule
import shared.document.operations.media.modules.ImageDownloadModule
import shared.document.operations.media.modules.VideoDownloadModule
import shared.saveable.SaveableHelper
import viewmodel.SettingsViewModel
import java.io.File

private val logger = KotlinLogging.logger { }
private val scope = CoroutineScope(Dispatchers.Default)

/**
 * [SaveableHelper] with [settings][SettingsViewModel] based values. Updates automatically on settings changes
 */
val Downloader = SaveableHelper(
    SettingsViewModel.apiTimeoutInSeconds.value,
    SettingsViewModel.entryTimeoutInSeconds.value,
    operationsBasedOnSettings(),
    File(SettingsViewModel.folderToSave.value ?: "")
)

private fun operationsBasedOnSettings(): Set<IProcessorOperation> {
    val necessaryFirstOperations = mutableSetOf(
        RemoveCssOperation,
        RemoveAdsOperation,
        CombineTemplateOperation,
        FormatHtmlOperation,
        ChangeTitleOperation,
    ).apply {
        val metadataOp = SaveMetadata
        val commentsOp = SaveCommentsOperation

        commentsOp.addCacheListener {
            logger.debug { "Got cache from save comments operation" }
            metadataOp.setCachedComments(it)
        }

        metadataOp.addCacheListener {
            logger.debug { "Got cache from metadata operation" }
            commentsOp.setCachedComments(it)
        }

        if (SettingsViewModel.saveMetadata.value) {
            add(metadataOp)
        }

        if (SettingsViewModel.saveComments.value) {
            add(commentsOp)
        }
    }

    val variableOperations = mutableListOf<IProcessorOperation>()

    val shouldDownloadImage = SettingsViewModel.downloadImage.value
    val shouldDownloadVideo = SettingsViewModel.downloadVideo.value

    val shouldAddMediaOperation = shouldDownloadImage || shouldDownloadVideo
    val shouldReplaceErrorMedia = SettingsViewModel.replaceErrorMedia.value
    val retryAmount = SettingsViewModel.retryAmount.value


    val saveMediaModules = mutableListOf<Pair<IDownloadModule, Boolean>>()

    saveMediaModules += ImageDownloadModule to shouldDownloadImage
    saveMediaModules += VideoDownloadModule to shouldDownloadVideo

    val timeoutMedia = SettingsViewModel.mediaTimeoutInSeconds.value
    val mediaOperation =
        SaveMediaOperation(saveMediaModules, retryAmount, shouldReplaceErrorMedia, timeoutMedia)

    val necessaryLastOperations = mutableSetOf(
        JavascriptAndCssOperation,
        SaveHtmlFileOperation(),
    )

    return necessaryFirstOperations + necessaryLastOperations
}

private val timeoutJobs =  listOf(
    SettingsViewModel.apiTimeoutInSeconds.onEach {
        Downloader.apiTimeoutInSeconds = it
    }.launchIn(scope),
    SettingsViewModel.entryTimeoutInSeconds.onEach {
        Downloader.entryTimeoutInSeconds
    }.launchIn(scope),
)

private val operationListeners = onEachMultipleState(setOf(
    SettingsViewModel.saveComments,
    SettingsViewModel.saveMetadata,
    SettingsViewModel.downloadImage,
    SettingsViewModel.downloadVideo,
    SettingsViewModel.mediaTimeoutInSeconds,
), scope) {
    Downloader.operations = operationsBasedOnSettings().toMutableSet()
}

private val folderJob = SettingsViewModel.folderToSave.onEach {
    Downloader.folderToSave = File(it ?: "")
}.launchIn(scope)

private fun onEachMultipleState(states: Set<StateFlow<Any>>, scope: CoroutineScope, action: () -> Unit): List<Job> {
    return states.map {
        it.onEach {
            action()
        }.launchIn(scope)
    }
}

