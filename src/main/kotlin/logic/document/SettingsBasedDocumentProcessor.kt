package logic.document

import kmtt.models.entry.Entry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import logic.abstracts.AbstractProgress
import logic.document.operations.*
import logic.document.operations.media.SaveMediaOperation
import logic.document.operations.media.modules.IDownloadModule
import logic.document.operations.media.modules.ImageDownloadModule
import logic.document.operations.media.modules.VideoDownloadModule
import org.jsoup.nodes.Document
import ui.viewmodel.SettingsViewModel
import util.progress.redirectTo
import java.io.File

/**
 * [Document processor][DocumentProcessor] that handles operations management based on [settings][SettingsViewModel]
 */
class SettingsBasedDocumentProcessor(
    val saveFolder: File,
    document: Document,
    val entry: Entry? = null
): AbstractProgress() {
    private val necessaryFirstOperations = listOf(
        RemoveCssOperation,
        FormatHtmlOperation,
        ChangeTitleOperation(entry)
    )

    private val necessaryLastOperations = listOf(
        SaveHtmlFileOperation(saveFolder)
    )

    private val processor = DocumentProcessor(document, saveFolder)

    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        // redirect progress
        processor.redirectTo(mutableProgress, scope)
    }

    fun updateOperations() {
        processor.clearOperationsQueue()

        val variableOperations = mutableListOf<IProcessorOperation>()

        val shouldDownloadImage = SettingsViewModel.downloadImage.value
        val shouldDownloadVideo = SettingsViewModel.downloadVideo.value

        val shouldAddMediaOperation = shouldDownloadImage || shouldDownloadVideo
        val shouldReplaceErrorMedia = SettingsViewModel.replaceErrorMedia.value
        val retryAmount = SettingsViewModel.retryAmount.value

        if (shouldAddMediaOperation) {
            val saveMediaModules = mutableListOf<IDownloadModule>()

            if (shouldDownloadImage) saveMediaModules.add(ImageDownloadModule)
            if (shouldDownloadVideo) saveMediaModules.add(VideoDownloadModule)

            val timeoutMedia = SettingsViewModel.mediaTimeoutInSeconds.value
            val mediaOperation = SaveMediaOperation(saveMediaModules, retryAmount, shouldReplaceErrorMedia, saveFolder, timeoutMedia)

            variableOperations.add(mediaOperation)
        }

        val queue = necessaryFirstOperations + variableOperations + necessaryLastOperations

        // I'm not sure if this is right method to do it.
        // Maybe there could be some performance issues with it
        // Or not because we don't have too many operations
        processor.addOperation(*queue.toTypedArray())
    }

    suspend fun process(): Document {
        updateOperations()
        return processor.process()
    }
}