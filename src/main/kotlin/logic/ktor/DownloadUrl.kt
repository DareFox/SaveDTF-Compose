package logic.ktor

import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.coroutines.*
import logic.cache.buildCache
import logic.document.operations.media.BinaryMedia
import logic.document.operations.media.MediaMetadata
import mu.KotlinLogging
import ui.i18n.Lang
import util.cache.getValueWithMetadata
import util.cache.setValueWithMetadata
import util.filesystem.convertToValidName
import util.kmttapi.getMediaIdOrNull

private val logger = KotlinLogging.logger { }
private val cache = buildCache()
private var timeout: Job? = null
private val scope = CoroutineScope(Dispatchers.IO)

suspend fun Client.downloadUrl(url: String, retryAmount: Int, replaceOnError: BinaryMedia? = null, timeoutInSeconds: Int): BinaryMedia {
    val mediaCacheID = convertToValidName(url.getMediaIdOrNull() ?: url)
    val cachedMedia = cache.getValueWithMetadata<MediaMetadata>(mediaCacheID)
    val client = this
    
    // Return cached version
    cachedMedia?.second?.let { meta ->
        return BinaryMedia(meta, cachedMedia.first)
    }

    var attemptCounter = 0

    // If url isn't cached -> download it
    do {
        timeout?.join() // Wait if there is too many requests
        val caught = kotlin.runCatching { // Catch exceptions with runCatching, try-catch don't work with coroutines
            val shouldUseTimeoutRestriction = timeoutInSeconds > 0
            val downloadJob: suspend () -> HttpResponse = {
                client.rateRequest<HttpResponse> {
                    attemptCounter++
                    method = HttpMethod.Get
                    url(url)
                    timeout {
                        connectTimeoutMillis = 250000
                        requestTimeoutMillis = 30000
                        socketTimeoutMillis = 30000
                    }
                }
            }

            if (shouldUseTimeoutRestriction) {
                withTimeout(timeoutInSeconds * 1000L) {
                    downloadJob()
                }
            } else {
                downloadJob()
            }
        }

        yield()
        val response = caught.getOrNull()

        if (response == null) {
            logger.error { "Response is null" }
            logger.error { "Caught exception: ${caught.exceptionOrNull()} " }
        }

        // Suspend all requests for 30 seconds on too many requests error
        if (response?.status ==  HttpStatusCode.TooManyRequests) {
            logger.info { "Too many requests. Delaying next requests to 30 seconds" }
            timeout = scope.launch {
                delay(30000L)
            }
        }
        yield()

        if (response?.status == HttpStatusCode.OK) {
            val binary = response.content.toByteArray()
            val type = response.contentType()

            requireNotNull(type) {
                "Content-type of response is null. Can't create BinaryMedia metadata"
            }

            val metadata = MediaMetadata(type.contentType, type.contentSubtype, mediaCacheID)
            yield()

            // Save to cache
            cache.setValueWithMetadata(mediaCacheID, binary, metadata)

            return BinaryMedia(metadata, binary)
        }

        // If retryAmount is bigger than 0, then try until reaching retryAmount
        // On retryAmount = 0: repeat request infinitely until success
        if (retryAmount != 0 && attemptCounter >= retryAmount) {
            // On retry limit, return replaceOnError (if possible)
            yield()
            replaceOnError?.let {
                return it
            }

            // If no replaceOnError media, throw exception
            if (response == null) {
                throw caught.exceptionOrNull() ?: RuntimeException(Lang.value.ktorServerNoResponse)
            } else {
                throw IllegalArgumentException(Lang.value.ktorErrorResponseStatus.format(response.status))
            }
        }

    } while(true)
}