package logic.document.operations.format.modules

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

object FormatGalleryModule : IHtmlFormatModule {
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
                processGallery(gallery)
            }

        return document
    }

    private fun processGallery(gallery: Element) {
        // Get data holder. If there's no data holder, then skip gallery
        val dataHolder = gallery.children().firstOrNull { child ->
            child.attr("name") == "gallery-data-holder"
        } ?: return

        // Parse it
        val rawJsonData = Json.parseToJsonElement(dataHolder.wholeText())

        // If data isn't JsonArray, then ignore it
        if (rawJsonData !is JsonArray) return

        val elements = convertJsonToElements(rawJsonData)
        val maxPreviewGallerySize = 5

        // Replace old gallery elements with new elements
        val newGallery = GalleryDOM(elements.size, maxPreviewGallerySize)
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

            val twoRowsLayout = elements.size >= 5
            if (index == 0 || (index == 1 && twoRowsLayout)) {
                newGallery.main.appendChild(div)
            } else {
                newGallery.sidebar.appendChild(div)
            }
        }
    }

    private fun convertJsonToElements(rawJsonData: JsonArray) = rawJsonData.map {
        json.decodeFromJsonElement<GalleryElement>(it)
    }.map {
        Element("div").also { div ->
            div.addClass("gall--item")
            div.attr("json-data-about-element", json.encodeToString(it))
            div.attr("media-url", "https://leonardo.osnova.io/${it.image.data.uuid}")

            // <br> || <br/> regex
            val breakLineRegex = """<\s*br\s*(/|)>""".toRegex()
            val cleanTitle = it.title.replace(breakLineRegex,"")

            div.attr("title", cleanTitle)

            val type = it.image.data.type
            val attrType = when {
                imageFormats.contains(type) -> "image"
                videoFormats.contains(type) -> "video"
                else -> "null"
            }

            div.attr("media-type", attrType)
        }
    }
}

internal class GalleryDOM(
    val size: Int,
    val maxPreviewGallerySize: Int,
): Element("div") {
    val sidebar = Element("div").also {
        it.addClass("gall--sidebar")
    }

    val main = Element("div").also {
        it.addClass("gall--main")
    }

    init {
        addClass("gall")

        // Limit gallery size in class to maximum of 5 elements
        // Required by CSS/JS gallery module:
        // https://github.com/sir-coffee-or-tea/darefox-dtf-saver-css
        addClass("gall--${size.coerceAtMost(maxPreviewGallerySize)}")

        addChildren(main, sidebar)
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


