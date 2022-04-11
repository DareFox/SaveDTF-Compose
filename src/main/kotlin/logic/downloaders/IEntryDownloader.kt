package logic.downloaders

import kmtt.models.entry.Entry
import kotlinx.coroutines.flow.StateFlow
import java.io.File

interface IEntryDownloader : IDownloader {
    val entry: Entry
}