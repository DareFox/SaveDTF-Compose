package logic.ktor

import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.*
import logic.cache.buildCache
import logic.document.operations.media.BinaryMedia
import mu.KotlinLogging
import ui.i18n.Lang
import util.string.sha256
import java.io.File
import java.io.InputStream

private val logger = KotlinLogging.logger { }
private val cache = buildCache()
private var timeout: Job? = null
private val scope = CoroutineScope(Dispatchers.IO)

suspend fun Client.downloadUrl(
    url: String,
    retryAmount: Int,
    replaceOnError: BinaryMedia? = null,
    timeoutInSeconds: Int,
    directory: File
): File? {
    val inputStream = cache.getValueOrNull(url) ?: download(
        timeoutInSeconds = timeoutInSeconds,
        url = url,
        retryAmount = retryAmount,
        replaceOnError = replaceOnError
    ) ?: return null

    val file: File = directory.resolve(url.sha256())

    withContext(Dispatchers.IO) {
        file.parentFile.mkdirs()
        file.createNewFile()
    }

    if (cache.containsKey(url)) {
        inputStream.copyTo(file.outputStream())
    } else {
        cache.setValue(url, inputStream, file.outputStream())
    }

    return file
}

private suspend fun download(
    url: String,
    retryAmount: Int,
    replaceOnError: BinaryMedia?,
    timeoutInSeconds: Int,
): InputStream? {
    var attemptCounter = 0
    do {
        timeout?.join() // Wait if there is too many requests
        val caught = runCatching { // Catch exceptions with runCatching, try-catch don't work with coroutines
            val shouldUseTimeoutRestriction = timeoutInSeconds > 0
            val downloadJob: suspend () -> HttpResponse = {
                yield()
                Client.rateRequest<HttpResponse> {
                    logger.debug {
                        "Sending rate request to $url"
                    }
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
        if (response?.status == HttpStatusCode.TooManyRequests) {
            logger.info { "Too many requests. Delaying next requests to 30 seconds" }
            timeout = scope.launch {
                delay(30000L)
            }
        }
        yield()

        if (response?.status == HttpStatusCode.OK) {
            val type = response.contentType()

            if (type == null) {
                logger.error { "Content-type of $url response is null. Can't create BinaryMedia metadata" }
                return null
            }

            return response.content.toInputStream()
        }

        // If retryAmount is bigger than 0, then try until reaching retryAmount
        // On retryAmount = 0: repeat request infinitely until success
        if (retryAmount != 0 && attemptCounter >= retryAmount) {
            if (response == null) {
                logger.error(caught.exceptionOrNull() ?: RuntimeException(Lang.value.ktorServerNoResponse))
            } else {
                logger.error(IllegalArgumentException(Lang.value.ktorErrorResponseStatus.format(response.status)))
            }

            return replaceOnError?.binary?.inputStream()
        }

    } while (true)
}

