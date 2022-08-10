package logic.document.operations.media

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import logic.document.AbstractProcessorOperation
import logic.document.operations.media.modules.IDownloadModule
import logic.ktor.Client
import logic.ktor.downloadUrl
import org.jsoup.nodes.Document
import ui.i18n.Lang
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

class SaveMediaOperation(
    val downloaderModules: List<IDownloadModule>,
    val retryAmount: Int,
    val replaceErrorMedia: Boolean,
    val saveFolder: File,
    val timeoutInSeconds: Int
) : AbstractProcessorOperation() {
    override val name: String = Lang.value.saveMediaOperation
    override suspend fun process(document: Document): Document {
        for (downloader in downloaderModules) {
            val toDownload = downloader.filter(document)
            val counter = MutableStateFlow(0)

            val counterJob = withContext(Dispatchers.Default) {
                launch { counter.onEach {
                    progress(
                        Lang.value.saveMediaDownloading.format(
                            downloader.downloadingContentType,
                            "$it/${toDownload.size}"
                        )
                    )
                }.collect() }
            }

            withContext(Dispatchers.IO) {
                toDownload.map { url ->
                    val job = async(context = CoroutineName("Media operation")) {
                        val errMedia = if (replaceErrorMedia) downloader.onErrorMedia else null
                        val media = Client.downloadUrl(url.second, retryAmount, errMedia, timeoutInSeconds) ?: return@async
                        val file = saveTo(media, downloader.folder ?: "")
                        val relativePath = file.relativeTo(saveFolder).path

                        yield()

                        downloader.transform(url.first, relativePath)
                    }

                    job.invokeOnCompletion {
                        counter.update { it + 1 }
                    }

                    job
                }.awaitAll()

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

    private suspend fun saveTo(media: BinaryMedia, folder: String): File {
        saveFolder.mkdirs()
        require(saveFolder.isDirectory) {
            "Parameter saveFolder is not a directory"
        }

        val filename = media.getFileName()
        val relativeFolder = saveFolder.resolve(folder).also { it.mkdirs() }

        val file = relativeFolder.resolve(filename)
        yield()
        file.writeBytes(media.binary)

        return file
    }
}