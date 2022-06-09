package logic.ktor

import io.github.resilience4j.kotlin.ratelimiter.executeSuspendFunction
import io.github.resilience4j.ratelimiter.RateLimiter
import io.github.resilience4j.ratelimiter.RateLimiterConfig
import io.github.resilience4j.ratelimiter.RateLimiterRegistry
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.time.Duration
import java.util.*

object Client {
    private val rateLimitID = UUID.randomUUID().toString()
    private val rateLimiterRegistry = RateLimiterRegistry.of(
        RateLimiterConfig
            .custom()
            .limitRefreshPeriod(Duration.ofSeconds(1))
            .limitForPeriod(3)
            .timeoutDuration(Duration.ofHours(300))
            .build()
    )

    val rateLimiter: RateLimiter = rateLimiterRegistry.rateLimiter(rateLimitID)
    val httpClient: HttpClient = HttpClient() {
        install(HttpTimeout)
        install(JsonFeature) {
            serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
            })
        }
    }
}

// limit coroutines amount at one time
private val semaphore = Semaphore(3, 0)

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
        rateLimiter.executeSuspendFunction {
            httpClient.request(block)
        }
    }
}