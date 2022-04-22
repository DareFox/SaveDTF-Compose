package logic.document.processors

import io.ktor.util.*
import kotlinx.coroutines.yield
import kotlinx.serialization.json.*
import logic.cache.buildCache
import logic.document.BinaryMedia
import logic.document.Resources
import mu.KotlinLogging
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.File

private val logger = KotlinLogging.logger { }
private val cache = buildCache()

// TODO check on coroutine cancellability

enum class MediaType {
    VIDEO,
    IMAGE
}

suspend fun Document.downloadDocument(
    progress: (String) -> Unit = {},
    retryAmount: Int,
    replaceError: Boolean,
    vararg mediaType: MediaType = listOf(MediaType.VIDEO, MediaType.IMAGE).toTypedArray(),
): MutableMap<String, ByteArray> {
    val allMap = mutableMapOf<String, ByteArray>()

    mediaType.toSet().forEach {
        yield()
        allMap += when (it) {
            MediaType.VIDEO -> this.saveVideos(progress, retryAmount, replaceError)
            MediaType.IMAGE -> this.saveImages(progress, retryAmount, replaceError)
        }
    }

    return allMap
}

private suspend fun Document.saveImages(
    progress: (String) -> Unit,
    retryAmount: Int,
    replaceError: Boolean,
): Map<String, ByteArray> {
    progress("Parsing image elements")

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


    return saveAndReplaceElement(
        folder = "img",
        elements = imageContainers + galleryImageContainers,
        progress = { progress("Image: $it") },
        retryAmount = retryAmount,
        errorReplace = if (replaceError) Resources.imageLoadFail else null
    ) { _, relativePath ->
        Element("img")
            .attr("src", relativePath)
            .attr("style", "height: 100%; width: 100%; object-fit: contain")
    }
}

private suspend fun Document.saveVideos(
    progress: (String) -> Unit,
    retryAmount: Int,
    replaceError: Boolean,
): Map<String, ByteArray> {
    progress("Parsing video elements")
    val videoContainers = getElementsByClass("andropov_video").filter {
        it.attr("data-video-mp4").isNotEmpty()
    }.map {
        it to it.attr("data-video-mp4")
    }

    return saveAndReplaceElement(
        folder = "video",
        elements = videoContainers,
        progress = { progress("Video: $it") },
        retryAmount = retryAmount,
        errorReplace = if (replaceError) Resources.videoLoadFail else null
    ) { _, relativePath ->
        val base = Element("video")
            .attr("controls", "")
            .attr("style", "height: 100%; width: 100%; object-fit: contain")
        val sourceElement = Element("source").attr("src", relativePath)

        base.prependChild(sourceElement)
    }
}

private suspend fun saveAndReplaceElement(
    folder: String,
    elements: List<Pair<Element, String>>,
    progress: (String) -> Unit = {},
    retryAmount: Int,
    errorReplace: BinaryMedia? = null,
    transform: (BinaryMedia, String) -> Element,
): MutableMap<String, ByteArray> {
    val elementPaths = mutableMapOf<String, ByteArray>()
    val resolvedFolder = File("").resolve(folder)
    val responses = downloadElementMedia(elements, progress, retryAmount, errorReplace)

    responses.forEach { (element, binaryMedia) ->
        // Delete all children from Element node
        // I think it could be done faster, if instead of remove children,
        // we will just remove parent node and recreate it with new children
        element.children().forEach {
            it.remove()
        }

        val mediaFile = resolvedFolder.resolve(binaryMedia.metadata.key + ".${binaryMedia.metadata.subtype}")
        val relativePath = mediaFile.relativeTo(File("")).path

        elementPaths[relativePath] = binaryMedia.binary

        element.prependChild(transform(binaryMedia, relativePath))
    }

    return elementPaths
}


