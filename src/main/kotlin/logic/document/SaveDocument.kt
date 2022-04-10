package logic.document

import logic.cache.buildCache
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

suspend fun Document.saveWith(
    path: File,
    vararg mediaType: MediaType = listOf(MediaType.VIDEO, MediaType.IMAGE).toTypedArray(),
) {
    val allMap = mutableMapOf<String, ByteArray>()

    mediaType.toSet().forEach {
        allMap += when (it) {
            MediaType.VIDEO -> this.saveVideos(path)
            MediaType.IMAGE -> this.saveImages(path)
        }
    }

    allMap.forEach { (relativePath, media) ->
        logger.info { "Saving file to $relativePath. File size: ${media.size} bytes" }
        path.resolve(relativePath).writeBytes(media)
    }

    logger.info { "Saving document to $path" }
    path.resolve("index.html").writeText(this.toString())

    logger.info { "Successfully saved document to ${path.resolve("index.html").absolutePath}" }

}

private suspend fun Document.saveImages(path: File): Map<String, ByteArray> {
    val imageContainers = getElementsByClass("andropov_image").filter {
        it.attr("data-image-src").isNotEmpty()
    }

    return saveBinaryElements(path, "img", imageContainers, "data-image-src") { _, relativePath ->
        Element("img")
            .attr("src", relativePath)
            .attr("style", "height: 100%; width: 100%; object-fit: contain")
    }
}

private suspend fun Document.saveVideos(path: File): Map<String, ByteArray> {
    val videoContainers = getElementsByClass("andropov_video").filter {
        it.attr("data-video-mp4").isNotEmpty()
    }

    return saveBinaryElements(path, "video", videoContainers, "data-video-mp4") { binary, relativePath ->
        val base = Element("video")
            .attr("controls", "")
            .attr("style", "height: 100%; width: 100%; object-fit: contain")
        val sourceElement = Element("source").attr("src", relativePath)

        base.prependChild(sourceElement)
    }
}

private suspend fun saveBinaryElements(
    path: File,
    folder: String,
    elements: List<Element>,
    attributeURL: String,
    transform: (BinaryMedia, String) -> Element,
): MutableMap<String, ByteArray> {
    val elementPaths = mutableMapOf<String, ByteArray>()
    val resolvedFolder = path.resolve(folder)
    val responses = downloadMedia(elements, attributeURL)

    resolvedFolder.mkdirs() // Create subfolder(s)

    responses.forEach { (element, binaryMedia) ->
        // Delete all children from Element node
        // I think it could be done faster, if instead of remove children,
        // we will just remove parent node and recreate it with new children
        element.children().forEach {
            it.remove()
        }

        val mediaFile = resolvedFolder.resolve(binaryMedia.metadata.key + ".${binaryMedia.metadata.subtype}")
        val relativePath = mediaFile.relativeTo(path).path

        elementPaths[relativePath] = binaryMedia.binary

        element.prependChild(transform(binaryMedia, relativePath))
    }

    return elementPaths
}


