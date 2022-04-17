package logic.downloaders

import io.ktor.client.features.*
import kmtt.models.entry.Entry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.yield
import logic.document.processors.*
import logic.downloaders.exceptions.NoContentDownloadedException
import mu.KotlinLogging
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import ui.viewmodel.SettingsViewModel
import util.getTempCacheFolder
import java.io.File
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger { }

private class EntryDownloader(override val entry: Entry, val retryAmount: Int, val replaceErrorMedia: Boolean) : IEntryDownloader {
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

                entry.title?.also {
                    progress("Change title")
                    document = document.changeTitle(it)
                    yield()
                }

                progress("Download all media")
                val downloadMode = mutableListOf<MediaType>()

                if (SettingsViewModel.downloadImage.value) {
                    downloadMode += MediaType.IMAGE
                }

                if (SettingsViewModel.downloadVideo.value) {
                    downloadMode += MediaType.VIDEO
                }

                files = document.downloadDocument(progress, retryAmount, replaceErrorMedia)
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

private val loaders = ConcurrentHashMap<EntryDownloaderParams, IEntryDownloader>()

fun entryDownloader(entry: Entry, retryAmount: Int = 5, replaceErrorMedia: Boolean = true): IEntryDownloader {
    val params = EntryDownloaderParams(entry, retryAmount, replaceErrorMedia)

    return loaders[params] ?: synchronized(entry) {
        loaders[params] ?: EntryDownloader(entry, retryAmount, replaceErrorMedia).also {
            loaders[params] = it
        }
    }
}

private data class EntryDownloaderParams(val entry: Entry, val retryAmount: Int, val replaceErrorMedia: Boolean)