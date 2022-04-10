package logic.document

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.coroutines.*
import logic.cache.buildCache
import logic.ktor.Client
import logic.ktor.rateRequest
import mu.KotlinLogging
import org.jsoup.nodes.Element
import util.getMediaId
import util.getValueWithMetadata
import util.setValueWithMetadata

private val logger = KotlinLogging.logger { }
private val cache = buildCache()

internal suspend fun downloadMedia(
    elements: List<Element>,
    attributeMediaURL: String,
): MutableMap<Element, BinaryMedia> {
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
                    val metadata = MediaMetadata(type.contentType, type.contentSubtype, mediaID)

                    cache.setValueWithMetadata(mediaID, byteArray, metadata)
                    logger.info { "Saved to cache" }

                    BinaryMedia(metadata, byteArray) // return is here


                } else {
                    throw IllegalArgumentException("Server responded with ${response.status} status")
                }
            }

            downloadMap[it] = media //  add result
        }
    }

    downloadJobs.joinAll() // Wait all downloads
    logger.info { "All download jobs finished. Returning map of results" }
    return downloadMap
}
