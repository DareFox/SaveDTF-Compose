package logic.document.operations.media.modules

import logic.document.operations.media.BinaryMedia
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/**
 * Module of [MediaProcessor] that downloads binary media.
 */
sealed interface IDownloadModule {
    /**
     * If folder is null, [MediaProcessor] will save media to folder with html
     */
    val folder: String?

    /**
     * On error, replace with this media
     */
    val onErrorMedia: BinaryMedia?

    /**
     *  Convert and filter elements which need to be downloaded.
     *  @return pair of [Element] and it's media url
     */
    fun filter(document: Document): List<Pair<Element, String>>

    /**
     *  Transform element to use relativePath instead of url
     */
    fun transform(element: Element, relativePath: String)

    /**
     *  What processor will download, e.g: Image, Video. Used in UI to show what media is downloading
     */
    val downloadingContentType: String
}