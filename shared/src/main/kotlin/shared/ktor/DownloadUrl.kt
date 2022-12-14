package shared.ktor

import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.*
import shared.document.operations.media.BinaryMedia
import mu.KotlinLogging
import shared.i18n.Lang
import shared.util.string.sha256
import java.io.File
import java.io.InputStream

private val logger = KotlinLogging.logger { }
private var timeout: Job? = null
private val scope = CoroutineScope(Dispatchers.IO)

suspend fun HttpClient.downloadUrl(
    url: String,
    retryAmount: Int,
    replaceOnError: BinaryMedia? = null,
    timeoutInSeconds: Int,
    directory: File
): File? {
    val inputStream = cache.getValueOrNull(url) ?: getInputSteamFromUrl(
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

private suspend fun HttpClient.getInputSteamFromUrl(
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
                this.rateRequest<HttpResponse> {
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

        when (response?.status) {
            // Log error
            null -> {
                logger.error { "Response is null" }
                logger.error { "Caught exception: ${caught.exceptionOrNull()} " }
            }
            // Suspend all requests for 30 seconds on too many requests error
            HttpStatusCode.TooManyRequests -> {
                logger.info { "Too many requests. Delaying next requests to 30 seconds" }
                timeout = scope.launch {
                    delay(30000L)
                }
            }
            HttpStatusCode.OK -> {
                val type = response.contentType()

                if (type == null) {
                    logger.error { "Content-type of $url response is null. Can't create BinaryMedia metadata" }
                    return null
                }

                return response.content.toInputStream()
            }
        }
        yield()

        // If retryAmount is bigger than 0, then try until attemptCounter becomes equal to retryAmount
        // If retryAmount is 0 or less, repeat request infinitely until success
        if (retryAmount in 1..attemptCounter) {
            if (response == null) {
                logger.error(caught.exceptionOrNull() ?: RuntimeException(Lang.ktorServerNoResponse))
            } else {
                logger.error(IllegalArgumentException(Lang.ktorErrorResponseStatus.format(response.status)))
            }

            return replaceOnError?.binary?.inputStream()
        }
    } while (true)
}

