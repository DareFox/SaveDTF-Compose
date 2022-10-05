package shared.util.coroutine

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel

/**
 * Run block of suspending code, and after it cancel a job
 *
 * @return result of block of suspending code
 */
suspend fun <T> Job.cancelOnSuspendEnd(block: suspend () -> T): T {
    val result = block()
    this.cancel("Block of code finished a job")
    return result
}

/**
 * Run block of code, and after it cancel a job
 *
 * @return result of block of code
 */
fun <T> Job.cancelOnEnd(block: () -> T): T {
    val result = block()
    this.cancel("Block of code finished a job")
    return result
}