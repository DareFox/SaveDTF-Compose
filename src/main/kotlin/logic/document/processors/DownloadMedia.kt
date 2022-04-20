package logic.document.processors

import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import logic.cache.buildCache
import logic.document.BinaryMedia
import logic.document.MediaMetadata
import logic.ktor.Client
import logic.ktor.rateRequest
import mu.KotlinLogging
import org.jsoup.nodes.Element
import util.getMediaId
import util.getValueWithMetadata
import util.setValueWithMetadata

private val logger = KotlinLogging.logger { }
private val cache = buildCache()

internal suspend fun downloadElementMedia(
    elements: List<Element>,
    attributeMediaURL: String,
    progress: (String) -> Unit,
    retryAmount: Int,
    replaceError: BinaryMedia?
): MutableMap<Element, BinaryMedia> {
    val downloaderScope = CoroutineScope(Dispatchers.IO)
    val downloadJobs = mutableListOf<Job>()
    val finishedJobs = MutableStateFlow<Int>(0)
    val downloadMap = mutableMapOf<Element, BinaryMedia>()

    try {
        if (elements.isNotEmpty()) {
            val progressJob: Job = downloaderScope.launch {
                finishedJobs.collect {
                    progress("Downloaded $it of ${elements.size}")
                }
            }

            elements.forEach {
                val downloadUrl = it.attr(attributeMediaURL)
                val mediaID = downloadUrl.getMediaId()

                // Download media concurrently
                val media = downloadMediaAsync(downloaderScope = downloaderScope,
                    mediaID = mediaID,
                    downloadUrl = downloadUrl,
                    retryAmount = retryAmount,
                    replaceError = replaceError
                ).await()

                yield()
                downloadMap[it] = media
                finishedJobs.update { counter -> counter + 1 }
            }


            progressJob.cancel()
            progress("All elements are downloaded")
            logger.info { "All download jobs finished. Returning map of results" }
        }
    } catch (cancelled: CancellationException) {
        downloadJobs.forEach {
            it.cancel(cancelled)
        }
        throw cancelled
    }
    return downloadMap
}

suspend fun downloadMediaAsync(
    mediaID: String,
    downloadUrl: String,
    retryAmount: Int,
    downloaderScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    replaceError: BinaryMedia? = null
) = downloaderScope.async {
    val cached = cache.getValueWithMetadata<MediaMetadata>(mediaID)

    val media = if (cached?.second != null) {
        logger.info { "$downloadUrl is cached. Returning it" }
        BinaryMedia(cached.second!!, cached.first)
    } else {
        logger.info { "No cache. Downloading media from $downloadUrl" }
        var counter = 0
        val binaryMedia: BinaryMedia

        do {
            val response: HttpResponse? = runBlocking {
                kotlin.runCatching { // Catch exceptions with runCatching, try-catch don't work with coroutines
                    Client.rateRequest<HttpResponse> {
                        method = HttpMethod.Get
                        url(downloadUrl)
                        timeout {
                            connectTimeoutMillis = 250000
                            requestTimeoutMillis = 30000
                            socketTimeoutMillis = 30000
                        }
                    }
                }.getOrNull()
            }
            counter++

            if (response?.status == HttpStatusCode.OK) {
                logger.info { "Downloaded $downloadUrl" }
                val byteArray = response.content.toByteArray()
                val type = response.contentType()!!
                val metadata = MediaMetadata(type.contentType, type.contentSubtype, mediaID)

                cache.setValueWithMetadata(mediaID, byteArray, metadata)
                logger.info { "Saved to cache" }

                binaryMedia = BinaryMedia(metadata, byteArray) // return is here
                break;
            } else if (retryAmount != 0 && counter >= retryAmount) {
                // If retryAmount is bigger than 0, try until retryAmount
                // On retryAmount = 0 repeat infinitely until success
                if (replaceError != null) {
                    logger.info("Replacing error media...")
                    binaryMedia = replaceError
                    break;
                }
                // If Retry amount is < 0, throw error
                if (response == null) {
                    throw RuntimeException("No response from server or can't connect to server")
                } else {
                    throw IllegalArgumentException("Server responded with ${response.status} status")
                }
            }
        } while (true)
        binaryMedia
    }

    media // Return media
}
