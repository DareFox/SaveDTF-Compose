package logic.document

import logic.document.BinaryMedia
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/**
 * Module of [DocumentProcessor]
 */
sealed interface IDownloadProcessor {
    /**
     * If folder is null, [DocumentProcessor] will save media to folder with html
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
    fun transform(element: Element, relativePath: String): Element

    /**
     *  What processor will download, e.g: Image, Video
     */
    val downloadingContentType: String
}