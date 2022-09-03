package logic.document

import kmtt.models.entry.Entry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import logic.abstracts.AbstractProgress
import logic.document.operations.*
import logic.document.operations.format.FormatHtmlOperation
import logic.document.operations.media.SaveMediaOperation
import logic.document.operations.media.modules.IDownloadModule
import logic.document.operations.media.modules.ImageDownloadModule
import logic.document.operations.media.modules.VideoDownloadModule
import mu.KotlinLogging
import org.jsoup.nodes.Document
import viewmodel.SettingsViewModel
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
    private val logger = KotlinLogging.logger {  }

    private val necessaryFirstOperations = mutableListOf(
        RemoveCssOperation,
        RemoveAdsOperation,
        CombineTemplateOperation,
        FormatHtmlOperation,
        ChangeTitleOperation(entry),
    ).apply {
        entry?.let {
            val metadataOp = SaveMetadata(it, saveFolder)
            val commentsOp = SaveCommentsOperation(it)

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
    }



    private val necessaryLastOperations = mutableListOf(
        JavascriptAndCssOperation,
        SaveHtmlFileOperation(saveFolder),
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


        val saveMediaModules = mutableListOf<Pair<IDownloadModule, Boolean>>()

        saveMediaModules += ImageDownloadModule to shouldDownloadImage
        saveMediaModules += VideoDownloadModule to shouldDownloadVideo

        val timeoutMedia = SettingsViewModel.mediaTimeoutInSeconds.value
        val mediaOperation = SaveMediaOperation(saveMediaModules, retryAmount, shouldReplaceErrorMedia, saveFolder, timeoutMedia)

        variableOperations.add(mediaOperation)

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