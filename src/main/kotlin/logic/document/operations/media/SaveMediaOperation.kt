package logic.document.operations.media

import kotlinx.coroutines.yield
import logic.document.AbstractProcessorOperation
import logic.document.operations.media.modules.IDownloadModule
import logic.ktor.Client
import logic.ktor.downloadUrl
import org.jsoup.nodes.Document
import java.io.File

class SaveMediaOperation(
    val downloaderModules: List<IDownloadModule>,
    val retryAmount: Int,
    val replaceErrorMedia: Boolean,
    val saveFolder: File
): AbstractProcessorOperation() {
    override val name: String = "Media saver"

    override suspend fun process(document: Document): Document {
        downloaderModules.forEach {downloader ->
            val toDownload = downloader.filter(document)

            toDownload.mapIndexed { index, url ->
                val prefix = "${downloader.downloadingContentType} ${index + 1}/${toDownload.size}"
                val errMedia = if (replaceErrorMedia) downloader.onErrorMedia else null
                val media = withProgressSuspend(
                    "$prefix: Downloading ${url.second}..."
                ) {
                    Client.downloadUrl(url.second, retryAmount, errMedia)
                }

                val file = withProgressSuspend("$prefix: Saving file...") {
                    saveTo(media, downloader.folder ?: "")
                }

                val relativePath = file.relativeTo(saveFolder).path
                withProgress("$prefix: Transforming document...") {
                    downloader.transform(url.first, relativePath)
                }
            }
        }

        withProgressSuspend("Saving index.html...") {
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