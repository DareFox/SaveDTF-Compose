package logic.document.processors

import kotlinx.coroutines.yield
import logic.cache.buildCache
import logic.document.BinaryMedia
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
    vararg mediaType: MediaType = listOf(MediaType.VIDEO, MediaType.IMAGE).toTypedArray(),
): MutableMap<String, ByteArray> {
    val allMap = mutableMapOf<String, ByteArray>()

    mediaType.toSet().forEach {
        yield()
        allMap += when (it) {
            MediaType.VIDEO -> this.saveVideos(progress)
            MediaType.IMAGE -> this.saveImages(progress)
        }
    }

    return allMap
}

private suspend fun Document.saveImages(progress: (String) -> Unit): Map<String, ByteArray> {
    progress("Parsing image elements")

    val imageContainers = getElementsByClass("andropov_image").filter {
        it.attr("data-image-src").isNotEmpty()
    }

    return saveAndReplaceElement(
        folder = "img",
        elements = imageContainers,
        attributeURL = "data-image-src",
        progress = { progress("Image: $it") }
    ) { _, relativePath ->
        Element("img")
            .attr("src", relativePath)
            .attr("style", "height: 100%; width: 100%; object-fit: contain")
    }
}

private suspend fun Document.saveVideos(progress: (String) -> Unit): Map<String, ByteArray> {
    progress("Parsing video elements")
    val videoContainers = getElementsByClass("andropov_video").filter {
        it.attr("data-video-mp4").isNotEmpty()
    }

    return saveAndReplaceElement(
        folder = "video",
        elements = videoContainers,
        attributeURL = "data-video-mp4",
        progress = { progress("Video: $it") }
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
    elements: List<Element>,
    attributeURL: String,
    progress: (String) -> Unit = {},
    transform: (BinaryMedia, String) -> Element,
): MutableMap<String, ByteArray> {
    val elementPaths = mutableMapOf<String, ByteArray>()
    val resolvedFolder = File("").resolve(folder)
    val responses = downloadMedia(elements, attributeURL, progress)

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


