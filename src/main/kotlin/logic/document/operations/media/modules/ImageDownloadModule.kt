package logic.document.operations.media.modules

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.*
import logic.document.operations.media.BinaryMedia
import logic.document.operations.media.Resources
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import ui.i18n.Lang
import util.dom.recreateWithoutNodes

object ImageDownloadModule: IDownloadModule {
    override val folder: String = "img"
    override val onErrorMedia: BinaryMedia? = Resources.imageLoadFail

    override fun filter(document: Document): List<Pair<Element, String>> {
        return getImageContainers(document) + getGalleryImageContainers(document)
    }

    private fun getImageContainers(document: Document): List<Pair<Element, String>> {
        return document.getElementsByClass("andropov_image").filter {
            // Check if element has link and is div
            // Why check div?
            // Because regular images are in container, but images of quotes aren't
            it.attr("data-image-src").isNotEmpty() && it.tagName() == "div"
        }.map { div ->
            val img = Element("img").also {
                // Set css style to img
                it.attr("style", "object-fit: contain; height: 100%; max-width: 100%")
            }

            val newDiv = div.recreateWithoutNodes()
            newDiv.appendChild(img)

            // return element and link to media
            img to newDiv.attr("data-image-src")
        }
    }

    private fun getGalleryImageContainers(document: Document): List<Pair<Element, String>> {
        // Convert each gallery items to list of pairs
        return document.getElementsByClass("gall--item").mapNotNull { div ->
            // If type or url is empty, then skip it by returning null
            val type = div.attr("media-type").ifEmpty { return@mapNotNull null  }
            val url = div.attr("media-url").ifEmpty { return@mapNotNull null  }

            if (type != "image") return@mapNotNull null

            div to url
        }
    }
    override fun transform(element: Element, relativePath: String) {
        if (element.tagName() == "div") {
            element.attr("style", createBackgroundImageStyleValue(relativePath))
        } else {
            // Assuming it's img element
            element.attr("src", relativePath)
        }
    }

    override val downloadingContentType: String
        get() = Lang.value.downloadImageType

    /**
     * Create background-image value for style tag
     *
     * Example:
     * ```
     * "background-image: url('[YOUR FILEPATH HERE]')"
     * ```
     */
    private fun createBackgroundImageStyleValue(filePath: String): String {
        val slashRegex = """\\""".toRegex()

        // replace backslashes to forward slashes
        return "background-image: url('${filePath.replace(slashRegex, "/")}')"
    }
}
