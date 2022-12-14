package shared.ktor

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import kotlinx.coroutines.sync.withPermit
import shared.io.RateLimitSemaphore
import kotlin.time.DurationUnit
import kotlin.time.toDuration

object Client {
    val httpClient: HttpClient = HttpClient {
        install(HttpTimeout)
        install(JsonFeature) {
            serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
            })
        }
    }
}

// limit coroutines amount at one time
private val semaphore = RateLimitSemaphore(3, 0, 1.toDuration(DurationUnit.SECONDS))

// Hide semaphore by function
suspend fun <T> withSemaphore(block: suspend () -> T): T {
    return semaphore.withPermit {
        block()
    }
}

/**
 * Send request with rate limit of 3 usages in 1 second and thread limit of 3 coroutines/threads
 */
suspend inline fun <reified T> Client.rateRequest(crossinline block: HttpRequestBuilder.() -> Unit): T {
    return withSemaphore {
        httpClient.request(block)
    }
}