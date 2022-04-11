package logic.downloaders

import kotlinx.coroutines.flow.StateFlow
import java.io.File

interface IDownloader {
    val isDownloaded: StateFlow<Boolean>

    suspend fun download(progress: (String) -> Unit): Boolean

    suspend fun save(file: File)
}