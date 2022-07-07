package logic.document.operations.media

import kotlinx.coroutines.yield
import logic.document.AbstractProcessorOperation
import logic.document.operations.media.modules.IDownloadModule
import logic.ktor.Client
import logic.ktor.downloadUrl
import org.jsoup.nodes.Document
import ui.i18n.Lang
import java.io.File

class SaveMediaOperation(
    val downloaderModules: List<IDownloadModule>,
    val retryAmount: Int,
    val replaceErrorMedia: Boolean,
    val saveFolder: File,
    val timeoutInSeconds: Int
): AbstractProcessorOperation() {
    override val name: String = Lang.value.saveMediaOperation
    override suspend fun process(document: Document): Document {
        downloaderModules.forEach {downloader ->
            val toDownload = downloader.filter(document)

            toDownload.mapIndexed { index, url ->
                val prefix = "${downloader.downloadingContentType} ${index + 1}/${toDownload.size}"
                val errMedia = if (replaceErrorMedia) downloader.onErrorMedia else null
                val media = withProgressSuspend(
                    Lang.value.saveMediaDownloading.format(prefix, url.second)
                ) {
                    Client.downloadUrl(url.second, retryAmount, errMedia, timeoutInSeconds)
                }

                val file = withProgressSuspend(Lang.value.savingFile.format(prefix)) {
                    saveTo(media, downloader.folder ?: "")
                }

                val relativePath = file.relativeTo(saveFolder).path
                withProgress(Lang.value.transformingDocument.format(prefix)) {
                    downloader.transform(url.first, relativePath)
                }
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