package logic.document.modules

import logic.document.BinaryMedia
import logic.document.Resources
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import util.recreateWithoutNodes

object VideoDownloadModule: IDownloadModule {
    override val folder: String = "video"
    override val onErrorMedia: BinaryMedia? = Resources.videoLoadFail

    override fun filter(document: Document): List<Pair<Element, String>> {
        return document.getElementsByClass("andropov_video").filter {
            it.attr("data-video-mp4").isNotEmpty()
        }.map {
            it to it.attr("data-video-mp4")
        }
    }

    override fun transform(element: Element, relativePath: String) {
        val sourceElement = Element("source").attr("src", relativePath)

        val base = Element("video")
            .attr("controls", "")
            .attr("style", "height: 100%; width: 100%; object-fit: contain")
            .prependChild(sourceElement)


        element
            .recreateWithoutNodes()
            .prependChild(base)
    }

    override val downloadingContentType: String
        get() = "Video"
}