package logic.document.operations.media.modules

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.*
import logic.document.operations.media.BinaryMedia
import logic.document.operations.media.Resources
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
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
                it.attr("style", "object-fit: contain; width: 100%; height: 100%;")
            }

            val newDiv = div.recreateWithoutNodes()
            newDiv.appendChild(img)

            // return element and link to media
            img to newDiv.attr("data-image-src")
        }
    }

    private fun getGalleryImageContainers(document: Document): List<Pair<Element, String>> {
        return document.getElementsByClass("gall").mapNotNull { gallery ->
            val dataHolder = gallery.children().firstOrNull { child -> child.attr("name") == "gallery-data-holder" }

            // If gallery data exists
            dataHolder?.let { holder ->
                // Parse it
                val data = Json.parseToJsonElement(holder.wholeText())

                if (data is JsonArray) {
                    val elements = mutableListOf<Pair<Element, String>>()

                    // Convert data object to images ID
                    data.forEach { element ->
                        try {
                            val id = element.jsonObject["image"]?.jsonObject?.get("data")?.jsonObject?.get("uuid")
                            val url = id?.jsonPrimitive?.toString()?.let {
                                // toString returns ""112032103012"", so we need to remove this quotes
                                val trimmed = """(^"|"${'$'})""".toRegex().replace(it, "")

                                "https://leonardo.osnova.io/$trimmed"
                            }

                            url?.let {
                                elements += Element("div") to url
                            }
                        } catch (_: Exception) {}
                    }

                    // Limit gallery size in class to maximum of 5 elements
                    // Required by CSS/JS gallery module:
                    // https://github.com/sir-coffee-or-tea/darefox-dtf-saver-css
                    val maxPreviewGallerySize = 5
                    val gallerySizeClass = "gall--${elements.size.coerceAtMost(maxPreviewGallerySize)}"

                    // Replace old gallery elements with new elements
                    val newGallery = Element("div").addClass("gall").addClass(gallerySizeClass)
                    gallery.replaceWith(newGallery)

                    elements.forEachIndexed { index, pair ->
                        val div = pair.first.also {
                            it.attr("pos", index.toString())

                            // On fifth (based on maxPreviewGallerySize) preview element show +n of remaining images
                            // Ignore if fifth element is the last element in array, to prevent "+0" text on preview
                            if (index == maxPreviewGallerySize - 1 && elements.size > maxPreviewGallerySize) {
                                val remainingSize = elements.size - maxPreviewGallerySize
                                it.attr("data-more", "+$remainingSize")
                            } else {
                                // To remove black transparent overlay on last gallery preview element
                                // data-more attribute should be empty like this: ""
                                it.attr("data-more", "")
                            }
                        }

                        newGallery.appendChild(div)
                    }

                    elements.toList() // make it immutable
                } else {
                    null
                }
            }
        }.flatten()
    }
    override fun transform(element: Element, relativePath: String) {
        if (element.tagName() == "div") {
            element.attr("style", createBackgroundImageStyleValue(relativePath))
        } else {
            // Assuming it's img element
            element.attr("src", relativePath)
        }
    }

    override val downloadingContentType: String = "Image"

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
