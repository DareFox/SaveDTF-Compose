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


            dataHolder?.let { holder ->
                val data = Json.parseToJsonElement(holder.wholeText())

                if (data is JsonArray) {
                    val elements = mutableListOf<Pair<Element, String>>()

                    data.forEach { element ->
                        try {
                            val id = element.jsonObject["image"]?.jsonObject?.get("data")?.jsonObject?.get("uuid")
                            val url = id?.jsonPrimitive?.toString()?.let {
                                // toString returns ""112032103012"", so we need to remove this quotes
                                val trimmed = """(^"|"${'$'})""".toRegex().replace(it, "")

                                "https://leonardo.osnova.io/$trimmed"
                            }

                            url?.let {
                                elements += Element("img") to url
                            }
                        } catch (_: Exception) {}
                    }

                    // Replace old gallery elements with new elements
                    val newGallery = Element("div").addClass("gall")
                    gallery.replaceWith(newGallery)

                    elements.forEachIndexed { index, it ->
                        val img = it.first.also {
                            // Set css style to img
                            it.attr("style", "object-fit: contain; width: 100%; height: 100%;")
                        }

                        val div = Element("div").also {
                            it.attr("pos", index.toString())
                            it.appendChild(img)
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
        element.attr("src", relativePath)
    }

    override val downloadingContentType: String = "Image"
}
