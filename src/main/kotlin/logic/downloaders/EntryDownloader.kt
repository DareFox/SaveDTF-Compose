package logic.downloaders

import io.ktor.client.features.*
import kmtt.models.entry.Entry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.yield
import logic.document.processors.downloadDocument
import logic.document.processors.reformat
import logic.document.processors.removeStyles
import logic.downloaders.exceptions.NoContentDownloadedException
import mu.KotlinLogging
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import util.getTempCacheFolder
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.log

private val logger = KotlinLogging.logger { }

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
            logger.info { "Download of entry (id: ${entry.id}) was cancelled" }
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
            // TODO add synchronized version of save method (???)
            val binaryFiles = files!!.toMutableMap()
            val writtenFiles = mutableListOf<File>()

            try {
                yield()
                binaryFiles["index.html"] = document.toString().toByteArray() // Include binary HTML too

                binaryFiles.forEach { (path, binary) ->
                    yield()

                    val toWrite = file.resolve(path)
                    toWrite.parentFile.mkdirs() // Create dirs to file, but don't make binary file as new dir too

                    logger.info { "Writing ${binary.size}B to ${toWrite.absolutePath}" }
                    toWrite.writeBytes(binary)
                    writtenFiles += toWrite
                }

            } catch (cancel: CancellationException) {
                logger.info { "Save operation of entry (id: ${entry.id}) was cancelled. Deleting all created files" }
                // Delete written files on cancellation
                writtenFiles.forEach {
                    logger.info { "Deleting ${it.absolutePath}..." }
                    it.delete()
                }
                logger.info { "All ${writtenFiles.size} files were deleted" }
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