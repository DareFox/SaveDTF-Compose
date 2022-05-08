package logic.document.modules

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.*
import logic.document.BinaryMedia
import logic.document.Resources
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import util.removeChildNodes

object ImageDownloadModule: IDownloadModule {
    override val folder: String? = "img"
    override val onErrorMedia: BinaryMedia? = Resources.imageLoadFail

    override fun filter(document: Document): List<Pair<Element, String>> {
        return document.run {
            val imageContainers = getElementsByClass("andropov_image").filter {
                it.attr("data-image-src").isNotEmpty()
            }.map {
                it to it.attr("data-image-src")
            }

            val galleryImageContainers = getElementsByClass("gall").mapNotNull { gallery ->
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
                                    elements += Element("div") to url
                                }
                            } catch (_: Exception) {}
                        }

                        // Replace old gallery elements with new elements
                        val newGallery = Element("div").addClass("gall")
                        gallery.replaceWith(newGallery)

                        elements.forEachIndexed { index, it ->
                            newGallery.appendChild(it.first.attr("pos", index.toString()))
                        }

                        elements.toList() // make it immutable
                    } else {
                        null
                    }
                }
            }.flatten()

            galleryImageContainers + imageContainers
        }
    }

    override fun transform(element: Element, relativePath: String) {
        val img = Element("img")
            .attr("src", relativePath)
            .attr("style", "height: 100%; width: 100%; object-fit: contain")

        element
            .removeChildNodes()
            .appendChild(img)
    }

    override val downloadingContentType: String = "Image"
}