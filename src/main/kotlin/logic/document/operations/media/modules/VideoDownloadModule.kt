package logic.document.operations.media.modules

import logic.document.operations.media.BinaryMedia
import logic.document.operations.media.Resources
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import util.dom.recreateWithoutNodes

object VideoDownloadModule: IDownloadModule {
    override val folder: String = "vid"
    override val onErrorMedia: BinaryMedia? = Resources.videoLoadFail

    override fun filter(document: Document): List<Pair<Element, String>> {
        return getGalleryVideoContainers(document) + getRegularVideoContainers(document)
    }

    private fun getRegularVideoContainers(document: Document): List<Pair<Element, String>> {
        return document.getElementsByClass("andropov_video").filter {
            it.attr("data-video-mp4").isNotEmpty()
        }.map {
            it to it.attr("data-video-mp4")
        }
    }

    private fun getGalleryVideoContainers(document: Document): List<Pair<Element, String>> {
        // Convert each gallery to list of pairs
        return document.getElementsByClass("gall").mapNotNull {
            // And to do this, convert their children to list of pairs
            it.children().mapNotNull childConvert@ { div ->
                // If type or url is empty, then skip it by returning null
                val type = div.attr("media-type").ifEmpty { return@childConvert null  }
                val url = div.attr("media-url").ifEmpty { return@childConvert null  }

                if (type != "video") return@childConvert null

                addVideoPreviewElement(div) to url
            }


        }.flatten() // Then flat all list of galleries to have one big list
    }

    private fun addVideoPreviewElement(div: Element): Element {
        val videoPreviewElement = Element("video").also {
            it.attr("autoplay", "")
            it.attr("muted", "")
            it.attr("loop", "")
            it.addClass("gall-vid-preview")
        }

        div.prependChild(videoPreviewElement)

        return videoPreviewElement
    }

    override fun transform(element: Element, relativePath: String) {
        if (element.hasClass("gall-vid-preview")) {
            element.attr("src", relativePath)
        } else {
            val sourceElement = Element("source").attr("src", relativePath)

            val base = Element("video")
                .attr("controls", "")
                .attr("style", "height: 100%; width: 100%; object-fit: contain")
                .prependChild(sourceElement)


            element
                .recreateWithoutNodes()
                .prependChild(base)
        }
    }

    override val downloadingContentType: String
        get() = "Video"
}