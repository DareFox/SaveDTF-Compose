package logic.ktor

import io.github.resilience4j.kotlin.ratelimiter.executeSuspendFunction
import io.github.resilience4j.ratelimiter.RateLimiter
import io.github.resilience4j.ratelimiter.RateLimiterConfig
import io.github.resilience4j.ratelimiter.RateLimiterRegistry
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import java.time.Duration
import java.util.*

object Client {
    private val rateLimitID = UUID.randomUUID().toString()
    private val rateLimiterRegistry = RateLimiterRegistry.of(RateLimiterConfig
        .custom()
        .limitRefreshPeriod(Duration.ofSeconds(1))
        .limitForPeriod(3)
        .timeoutDuration(Duration.ofHours(300))
        .build())

    val rateLimiter: RateLimiter = rateLimiterRegistry.rateLimiter(rateLimitID)
    val httpClient: HttpClient = HttpClient() {
        install(HttpTimeout)
    }
}

/**
 * Send request with rate limit
 */
suspend inline fun <reified T> Client.rateRequest(crossinline block: HttpRequestBuilder.() -> Unit): T {
    return rateLimiter.executeSuspendFunction {
        httpClient.request(block)
    }
}