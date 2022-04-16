package logic.downloaders

import kmtt.models.entry.Entry

interface IEntryDownloader : IDownloader {
    val entry: Entry
}