package shared.util.timeout

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
/**
 * Same as [withTimeout], but if timeout is negative or zero, no error will be thrown.
 */
suspend fun <R> Any.withPossibleTimeout(timeout: Duration, block: suspend CoroutineScope.() -> R): R {
    return if (timeout.isPositive()) {
        withTimeout(timeout, block)
    } else {
        coroutineScope {
            block()
        }
    }
}

/**
 * Same as [withTimeout], but if timeout is negative or zero, no error will be thrown.
 */
suspend fun <R> Any.withPossibleTimeout(timeoutInMillis: Long, block: suspend CoroutineScope.() -> R): R {
    return if (timeoutInMillis > 0) {
        withTimeout(timeoutInMillis, block)
    } else {
        coroutineScope {
            block()
        }
    }
}