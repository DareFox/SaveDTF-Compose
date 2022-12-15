package shared.util.loop

import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.internal.throwArrayMissingFieldException
import mu.KotlinLogging

private val logger = KotlinLogging.logger {  }

class AttemptLoopBuilder<T>(val amountOfAttempts: Int) {
    private var returned = false
    // Hacky way to generic nullability
    private val returnedValue = mutableListOf<T>()
    private var breaked = false

    fun loop(block: AttemptLoopBuilder<T>.() -> T): T {
        return runBlocking {
            loop(block)
        }
    }

    /**
     * Wrapper to **`throw Exception(message)`**
     *
     * Calling this function, mark current attempt as failed and starts attempt again
     */
    fun callContinue(message: String?): Nothing {
        throw Exception(message)
    }

    /**
     * Fail all attempts of loop immediately.
     */
    fun callBreak(): Nothing {
        breaked = true
        throw Exception()
    }

    fun callReturn(value: T): Nothing {
        returned = true
        returnedValue += value
        throw Exception()
    }

    suspend fun loop(block: suspend AttemptLoopBuilder<T>.() -> T): T {
        var attemptCounter = 0;
        while (true) {
            try {
                attemptCounter++
                return block()
            } catch (ex: Throwable) {
                if (returned) {
                    return returnedValue.first()
                }
                if (breaked) {
                    throw FailedLimitedLoopException(
                        attemptCounter,
                        "Loop break",
                        ex
                    )
                }
                if (amountOfAttempts > 0 && attemptCounter == amountOfAttempts) {
                    throw FailedLimitedLoopException(
                        attemptCounter,
                        "All $amountOfAttempts attempts failed",
                        ex
                    )
                }
            }
        }
    }
}

class FailedLimitedLoopException(val attempts: Int, message: String, val lastCaught: Throwable):
    Exception(message + "\nLast caught exception: [${lastCaught.javaClass.simpleName}]: ${lastCaught.message} ")

fun <T> attemptLoop(attempts: Int, block: AttemptLoopBuilder<T>.() -> T): T {
    return AttemptLoopBuilder<T> (attempts).loop(block)
}
suspend fun <T> attemptLoopSuspend(attempts: Int, suspendBlock: suspend AttemptLoopBuilder<T> .() -> T): T {
    return AttemptLoopBuilder<T>(attempts).loop(suspendBlock)
}

fun <T> tryAttemptLoop(attempts: Int, block: AttemptLoopBuilder<T>.() -> T): T? {
    return try {
        AttemptLoopBuilder<T>(attempts).loop(block)
    } catch (ex: FailedLimitedLoopException) {
        logger.error(ex)
        null
    }
}
suspend fun <T> tryAttemptLoopSuspend(attempts: Int, suspendBlock: suspend AttemptLoopBuilder<T> .() -> T): T? {
    return try {
        AttemptLoopBuilder<T>(attempts).loop(suspendBlock)
    } catch (ex: FailedLimitedLoopException) {
        logger.error(ex)
        null
    }
}

