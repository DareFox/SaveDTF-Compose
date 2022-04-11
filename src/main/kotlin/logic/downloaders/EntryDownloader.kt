package logic.downloaders

import io.ktor.client.features.*
import kmtt.models.entry.Entry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.yield
import logic.document.processors.downloadDocument
import logic.document.processors.reformat
import logic.document.processors.removeStyles
import logic.downloaders.exceptions.NoContentDownloadedException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import util.getTempCacheFolder
import java.io.File
import java.util.concurrent.ConcurrentHashMap

private class EntryDownloader(override val entry: Entry) : IEntryDownloader {
    private var document: Document
    private var files: Map<String, ByteArray>? = null

    private val _isDownloaded = MutableStateFlow(false)
    override val isDownloaded: StateFlow<Boolean> = _isDownloaded

    init {
        val html = entry.entryContent?.html

        requireNotNull(html) {
            "No html data in entry"
        }

        document = Jsoup.parse(html)
    }

    override suspend fun download(progress: (String) -> Unit): Boolean {
        try {
            _isDownloaded.value = try {
                val file = getTempCacheFolder()

                progress("Removing old css")
                document = document.removeStyles()
                yield()

                progress("Insert inside template")
                document = document.reformat()
                yield()

                progress("Download all media")
                files = document.downloadDocument(progress)
                yield()

                progress("Entry was successfully downloaded")
                true
            } catch (ex: HttpRequestTimeoutException) {
                progress("Timeout Request Error")
                false
            }

            return _isDownloaded.value
        } catch (cancel: CancellationException) {
             files = null
            _isDownloaded.value = false
            document = Jsoup.parse(entry.entryContent!!.html!!) // Force unwrap because class and variable are immutable (we check null in init already)
            throw cancel
        }
    }

    override suspend fun save(file: File) {
        if (files == null) {
            throw NoContentDownloadedException("No files were downloaded. Please call download() before save()")
        } else {
            // TODO add synchronized version of save method
            val binaryFiles = files!!
            val writtenFiles = mutableListOf<File>()

            try {
                binaryFiles.forEach { (path, binary) ->
                    yield()
                    val toWrite = file.resolve(path)
                    toWrite.parentFile.mkdirs() // Create dirs to file, but don't make binary file as new dir too
                    toWrite.writeBytes(binary)
                    writtenFiles += toWrite
                }

                yield()
                val index = file.resolve("index.html").also { writtenFiles += it }
                index.writeText(document.toString())
                yield()

            } catch (cancel: CancellationException) {
                // Delete written files on cancellation
                writtenFiles.forEach {
                    it.delete()
                }
                throw cancel
            }
        }
    }
}

private val loaders = ConcurrentHashMap<Entry, IEntryDownloader>()

fun entryDownloader(entry: Entry): IEntryDownloader {
    return loaders[entry] ?: synchronized(entry) {
        loaders[entry] ?: EntryDownloader(entry).also {
            loaders[entry] = it
        }
    }
}