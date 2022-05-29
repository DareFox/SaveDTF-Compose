package logic.document.modules

import logic.document.BinaryMedia
import logic.document.Resources
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element


object QuoteAuthor: IDownloadModule {
    override val folder: String = "img"
    override val onErrorMedia: BinaryMedia? = Resources.imageLoadFail

    override fun filter(document: Document): List<Pair<Element, String>> {
        return document.getElementsByClass("block-quote__author-photo").filter {
            it.attr("data-image-src").isNotEmpty()
        }.map {
            it to it.attr("data-image-src")
        }
    }

    override fun transform(element: Element, relativePath: String) {
        val img = Element("img")
            .attr("src", relativePath)
            .attr("style", "object-fit: contain")

        element.replaceWith(img)
    }

    override val downloadingContentType: String = "Image"
}