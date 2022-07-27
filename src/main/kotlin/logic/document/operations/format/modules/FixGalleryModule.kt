package logic.document.operations.format.modules

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

object FixGalleryModule : IHtmlFormatModule {
    private val json = Json { ignoreUnknownKeys = true }

    private val imageFormats = listOf(
        "png", "jpg", "jpeg", "bmp", "webp"
    )

    private val videoFormats = listOf(
        "webm", "mp4", "mkv", "mov", "avi", "wmv", "gif"
    )

    override suspend fun process(document: Document): Document {
        document
            .getElementsByClass("gall")
            .forEach { gallery ->
                // Get data holder. If there's no data holder, then skip gallery
                val dataHolder = gallery.children().firstOrNull { child ->
                    child.attr("name") == "gallery-data-holder"
                } ?: return@forEach

                // Parse it
                val rawJsonData = Json.parseToJsonElement(dataHolder.wholeText())

                // If data isn't JsonArray, then ignore it
                if (rawJsonData !is JsonArray) return@forEach

                val elements = rawJsonData.map {
                    json.decodeFromJsonElement<GalleryElement>(it)
                }.map {
                    Element("div").also { div ->
                        div.attr("json-data-about-element", json.encodeToString(it))
                        div.attr("media-url", "https://leonardo.osnova.io/${it.image.data.uuid}")

                        val type = it.image.data.type
                        val attrType = when {
                            imageFormats.contains(type) -> "image"
                            videoFormats.contains(type) -> "video"
                            else -> "null"
                        }

                        div.attr("media-type", attrType)
                    }
                }

                // Limit gallery size in class to maximum of 5 elements
                // Required by CSS/JS gallery module:
                // https://github.com/sir-coffee-or-tea/darefox-dtf-saver-css
                val maxPreviewGallerySize = 5
                val gallerySizeClass = "gall--${elements.size.coerceAtMost(maxPreviewGallerySize)}"

                // Replace old gallery elements with new elements
                val newGallery = Element("div").addClass("gall").addClass(gallerySizeClass)
                gallery.replaceWith(newGallery)

                elements.forEachIndexed { index, div ->
                    div.attr("pos", index.toString())

                    // On fifth (based on maxPreviewGallerySize) preview element show +n of remaining images
                    // Ignore if fifth element is the last element in array, to prevent "+0" text on preview
                    if (index == maxPreviewGallerySize - 1 && elements.size > maxPreviewGallerySize) {
                        val remainingSize = elements.size - maxPreviewGallerySize
                        div.attr("data-more", "+$remainingSize")
                    } else {
                        // To remove black transparent overlay on last gallery preview element
                        // data-more attribute should be empty like this: ""
                        div.attr("data-more", "")
                    }

                    newGallery.appendChild(div)
                }
            }

        return document
    }
}

@Serializable
data class GalleryElement(val title: String, val image: GalleryElementMedia)

@Serializable
data class GalleryElementMedia(
    val type: String,
    val data: GalleryElementMediaData
)

@Serializable
data class GalleryElementMediaData(
    val uuid: String,
    val width: Int,
    val height: Int,
    val size: Long,
    val type: String,
    val color: String,
    val hash: String
)


