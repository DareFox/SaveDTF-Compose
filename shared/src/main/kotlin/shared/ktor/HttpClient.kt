package shared.ktor

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.withPermit
import mu.KotlinLogging
import shared.abstracts.StreamCache
import shared.document.operations.media.BinaryMedia
import shared.i18n.Lang
import shared.io.RateLimitSemaphore
import shared.util.loop.tryAttemptLoopSuspend
import shared.util.string.sha256
import shared.util.timeout.withPossibleTimeout
import java.io.File
import java.io.InputStream
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class HttpClient(
    val cache: StreamCache
) {
    val ktor: io.ktor.client.HttpClient = HttpClient {
        install(HttpTimeout)
        install(JsonFeature) {
            serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
            })
        }
    }
    private var timeout: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)
    private val logger = KotlinLogging.logger { }

    // limit coroutines amount at one time
    private val semaphore = RateLimitSemaphore(3, 0, 1.toDuration(DurationUnit.SECONDS))

    // Hide semaphore by function
    suspend fun <T> withRatePermit(block: suspend () -> T): T {
        return semaphore.withPermit {
            block()
        }
    }

    /**
     * Send request with rate limit of 3 usages in 1 second and thread limit of 3 coroutines/threads
     */
    suspend inline fun <reified T> HttpClient.rateRequest(crossinline block: HttpRequestBuilder.() -> Unit): T {
        return withRatePermit {
            ktor.request(block)
        }
    }

    suspend fun downloadUrlToFile(
        url: String,
        retryAmount: Int,
        timeoutDuration: Duration,
        replaceOnError: BinaryMedia? = null,
        directory: File
    ): File? {
        val inputStream = cache.getValueOrNull(url) ?: getInputSteamFromUrl(
            timeoutDuration = timeoutDuration,
            url = url,
            retryAmount = retryAmount
        ) ?: replaceOnError?.binary?.inputStream() ?: return null

        val file: File = directory.resolve(url.sha256())

        withContext(Dispatchers.IO) {
            file.parentFile.mkdirs()
            file.createNewFile()
        }

        inputStream.copyTo(file.outputStream())
        return file
    }

    suspend fun getInputSteamFromUrl(
        url: String,
        retryAmount: Int,
        timeoutDuration: Duration,
    ): InputStream? {
        // Return cached version if exists
        cache.getValueOrNull(url)?.let { return it }

        return tryAttemptLoopSuspend(retryAmount) {
            timeout?.join()
            val result = request(url, timeoutDuration)
            val response = result.getOrNull()
            val caughtException = result.exceptionOrNull()

            when (response?.status) {
                // Suspend all requests for 30 seconds on too many requests error
                HttpStatusCode.TooManyRequests -> {
                    logger.info { "Too many requests. Delaying next requests to 30 seconds" }
                    timeout = scope.launch {
                        delay(30000L)
                    }
                    callContinue("Too many requests. Delaying next requests to 30 seconds")
                }

                HttpStatusCode.OK -> {
                    callReturn(response.content.toInputStream())
                }

                else -> {
                    caughtException?.let { throw it }

                    if (response == null) {
                        callContinue(Lang.ktorServerNoResponse)
                    } else {
                        callContinue(Lang.ktorErrorResponseStatus.format(response.status))
                    }
                }
            }
        }?.also {
            // Cache downloaded value
            cache.setValue(url, it)
        }
    }

    private suspend fun request(url: String, timeoutDuration: Duration): Result<HttpResponse> {
        val client = this

        // Catch exceptions with runCatching, try-catch don't work with coroutines
        return runCatching {
            withPossibleTimeout(timeoutDuration) {
                client.rateRequest {
                    logger.debug {
                        "Sending rate request to $url"
                    }
                    method = HttpMethod.Get
                    url(url)
                    timeout {
                        connectTimeoutMillis = 250000
                        requestTimeoutMillis = 30000
                        socketTimeoutMillis = 30000
                    }
                }
            }
        }
    }
}