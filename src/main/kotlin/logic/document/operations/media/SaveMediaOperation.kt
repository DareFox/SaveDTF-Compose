package logic.document.operations.media

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import logic.document.AbstractProcessorOperation
import logic.document.operations.media.modules.IDownloadModule
import logic.ktor.Client
import logic.ktor.downloadUrl
import mu.KotlinLogging
import org.jsoup.nodes.Document
import ui.i18n.Lang
import java.io.File

class SaveMediaOperation(
    /**
     * List of download modules pairs. On true, will download media from (given by downloader) URLs, on false - won't
     */
    val downloaderModules: List<Pair<IDownloadModule, Boolean>>,
    val retryAmount: Int,
    val replaceErrorMedia: Boolean,
    val saveFolder: File,
    val timeoutInSeconds: Int
) : AbstractProcessorOperation() {
    override val name: String = Lang.value.saveMediaOperation
    private val logger = KotlinLogging.logger {  }
    override suspend fun process(document: Document): Document {
        for (moduleIgnorePair in downloaderModules) {
            val downloader = moduleIgnorePair.first
            val doNotDownloadURL = !moduleIgnorePair.second

            val toDownload = downloader.filter(document)
            val counter = MutableStateFlow(0)

            withContext(Dispatchers.IO) {
                val counterJob = launch { counter.onEach {
                        progress(
                            Lang.value.saveMediaDownloading.format(
                                downloader.downloadingContentType,
                                "$it/${toDownload.size}"
                            )
                        )
                    }.collect()
                }

                if (doNotDownloadURL) {
                    logger.info { "Ignoring download ${downloader.downloadingContentType} operation. All media will be linked to original URL" }
                }

                toDownload.map { url ->
                    val job = async(context = CoroutineName("Media operation")) {
                        val relativePath = if (doNotDownloadURL) {
                            url.second
                        } else {
                            val errMedia = if (replaceErrorMedia) downloader.onErrorMedia else null
                            val downloaderFolder = downloader.folder

                            val folder = if (downloaderFolder == null) {
                                saveFolder
                            } else {
                                saveFolder.resolve(downloaderFolder)
                            }

                            val media = Client.downloadUrl(
                                url = url.second,
                                retryAmount = retryAmount,
                                replaceOnError = errMedia,
                                timeoutInSeconds = timeoutInSeconds,
                                directory = folder
                            ) ?: return@async

                            media.relativeTo(saveFolder).path
                        }

                        yield()

                        downloader.transform(url.first, relativePath)
                        if (!doNotDownloadURL) {
                            logger.info { "Saved ${url.second} (${downloader.downloadingContentType})" }
                        }
                    }

                    job.invokeOnCompletion {
                        counter.update { it + 1 }
                    }

                    job
                }.awaitAll()

                logger.info { "Finished downloading all ${downloader.downloadingContentType} media" }

                counterJob.cancel()
            }
        }

        withProgressSuspend(Lang.value.savingIndexHtml) {
            val indexHTML = saveFolder.mkdirs().let { saveFolder.resolve("index.html") }
            yield()
            indexHTML.writeBytes(document.toString().toByteArray())
        }

        return document
    }
}