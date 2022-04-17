package logic.downloaders

import kotlinx.coroutines.flow.StateFlow
import java.io.File

interface IDownloader {
    val isDownloaded: StateFlow<Boolean>
    val progress: StateFlow<String?>

    suspend fun download(): Boolean

    suspend fun save(file: File)
}