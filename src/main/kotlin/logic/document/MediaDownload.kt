package logic.document

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.coroutines.*
import logic.cache.RamCache
import logic.ktor.Client
import logic.ktor.rateRequest
import mu.KotlinLogging
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import util.getMediaId
import util.getValueWithMetadata
import util.setValueWithMetadata

private val logger = KotlinLogging.logger { }
private val cache = RamCache()

suspend fun Document.saveImages(): Document {
    val imageContainers = getElementsByClass("andropov_image").filter {
        it.attr("data-image-src").isNotEmpty()
    }

    downloadElements(imageContainers, "data-image-src") {
        Element("img").attr("src", it.toBase64HTML())
    }

    return this
}

suspend fun Document.saveVideos(): Document {
    val videoContainers = getElementsByClass("andropov_video").filter {
        it.attr("data-video-mp4").isNotEmpty()
    }

    downloadElements(videoContainers, "data-video-mp4") {
        val base = Element("video").attr("controls", "")
        val sourceElement = Element("source").attr("src", it.toBase64HTML())

        base.prependChild(sourceElement)
    }

    return this
}

private suspend fun downloadElements(elements: List<Element>, urlAttriubte: String, transform: (BinaryMedia) -> Element) {
    val responses = getMediaFromElements(elements, urlAttriubte)

    responses.forEach { (element, binaryMedia) ->
        // Delete all children from Element node
        // I think it could be done faster, if instead of remove children,
        // we will just remove parent node and recreate it with new children
        element.children().forEach {
            it.remove()
        }

        element.prependChild(transform(binaryMedia))
    }
}

private suspend fun getMediaFromElements(elements: List<Element>, attributeMediaURL: String): MutableMap<Element, BinaryMedia> {
    val downloaderScope = CoroutineScope(Dispatchers.IO)
    val downloadJobs = mutableListOf<Job>()
    val downloadMap = mutableMapOf<Element, BinaryMedia>()

    elements.forEach {
        val downloadUrl = it.attr(attributeMediaURL)
        val mediaID = downloadUrl.getMediaId()
        // Download media concurrently
        downloadJobs += downloaderScope.launch {
            val cached = cache.getValueWithMetadata<MediaMetadata>(mediaID)

            val media = if (cached?.second != null) {
                logger.info { "$downloadUrl is cached. Returning it" }
                BinaryMedia(cached.second!!, cached.first)
            } else {
                logger.info { "No cache. Downloading media from $downloadUrl" }
                val response: HttpResponse = Client.rateRequest {
                    method = HttpMethod.Get
                    url(downloadUrl)
                }

                if (response.status == HttpStatusCode.OK) {
                    logger.info { "Downloaded $downloadUrl" }
                    val byteArray = response.content.toByteArray()
                    val type = response.contentType()!!
                    val metadata = MediaMetadata(type.contentType, type.contentSubtype)

                    val media = BinaryMedia(metadata, byteArray)
                    downloadMap[it] = media

                    cache.setValueWithMetadata(mediaID, byteArray, metadata)
                    logger.info { "Saved to cache" }

                    media // return is here
                } else {
                    throw IllegalArgumentException("Server responded with ${response.status} status")
                }
            }
        }
    }

    downloadJobs.joinAll() // Wait all downloads
    logger.info { "All download jobs finished. Returning map of results" }
    return downloadMap
}

