package logic.document

import kotlinx.coroutines.yield
import logic.abstracts.AbstractProgress
import logic.document.modules.IDownloadModule
import logic.ktor.Client
import logic.ktor.downloadUrl
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import util.readResource
import java.io.File

class DocumentProcessor(document: Document, val saveFolder: File): AbstractProgress() {
    // read-only for public usage
    var document: Document = document
        private set;

    fun changeTitle(newTitle: String) {
        document.head().getElementsByTag("title").first()?.text(newTitle)
    }

    fun removeCSS() {
        document.getElementsByTag("style").forEach {
            it.remove()
        }
    }

    fun reformat() {
        val template = readResource("templates/entry.html").readText()

        val templateDocument = Jsoup.parse(template)

        val wrapper = templateDocument
            .getElementsByClass("savedtf-insert-here")
            .first()

        requireNotNull(wrapper) {
            "There's no wrapper in html template"
        }

        wrapper.appendChild(document.body())
        document = templateDocument
    }

    suspend fun saveDocument(downloaderModules: List<IDownloadModule>, retryAmount: Int, replaceErrorMedia: Boolean) {
        downloaderModules.forEach {downloader ->
            val toDownload = downloader.filter(document)

            toDownload.mapIndexed { index, url ->
                val prefix = "${downloader.downloadingContentType} ${index + 1}/${toDownload.size}"
                val errMedia = if (replaceErrorMedia) downloader.onErrorMedia else null
                val media = progressSuspend(
                    "$prefix: Downloading $url..."
                ) {
                    Client.downloadUrl(url.second, retryAmount, errMedia)
                }

                val file = progressSuspend("$prefix: Saving file...") {
                    saveTo(media, downloader.folder ?: "")
                }

                val relativePath = file.relativeTo(saveFolder).path
                progress("$prefix: Transforming document...") {
                    downloader.transform(url.first, relativePath)
                }
            }
        }

        progressSuspend("HTML: Saving index.html...") {
            val indexHTML = saveFolder.mkdirs().let { saveFolder.resolve("index.html") }
            yield()
            indexHTML.writeBytes(document.toString().toByteArray())
        }

        clearProgress()
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
