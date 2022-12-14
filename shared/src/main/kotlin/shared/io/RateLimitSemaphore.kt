package shared.io

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import java.lang.IllegalStateException
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

/**
 * [Semaphore], but [release of permit][Semaphore.release]
 * happens only every given duration
 */
class RateLimitSemaphore(
    private val permits: Int,
    acquiredPermits: Int,
    releaseEvery: Duration
) : Semaphore {
    private var stopped: Boolean = false;
    private var _releaseEvery = releaseEvery
    private val scope = CoroutineScope(Dispatchers.Default)
    private val countSemaphore = Semaphore(permits, acquiredPermits)
    private val semaphore = Semaphore(permits, acquiredPermits)
    private var jobCounter: Job = startJobCounter()

    /**
     * Current duration of releasing permits
     */
    val releaseEvery
        get() = _releaseEvery

    override val availablePermits: Int
        // Math.min, but kotlin way
        get() = countSemaphore.availablePermits.coerceAtMost(semaphore.availablePermits)

    @OptIn(ExperimentalTime::class)
    private fun startJobCounter(): Job {
        return scope.launch {
            while (true) {
                delay(releaseEvery)
                val toRelease = permits - availablePermits
                repeat(toRelease) {
                    countSemaphore.release()
                }
            }
        }
    }

    /**
     * Change duration of releasing permits
     *
     * **NOTE: It will not change current delay, changes will be applied after it**
     */
    fun changeDuration(duration: Duration) {
        _releaseEvery = duration
    }

    /**
     * Change duration of releasing permits
     *
     * **NOTE: It will not change current delay, changes will be applied after it**
     */
    fun changeDuration(durationInMs: Long) {
        _releaseEvery = durationInMs.toDuration(DurationUnit.MILLISECONDS)
    }


    override suspend fun acquire() {
        if (stopped) {
            throw IllegalStateException("RateLimitSemaphore is stopped")
        }

        countSemaphore.acquire()
        semaphore.acquire()
    }

    override fun release() {
        semaphore.release()
    }

    override fun tryAcquire(): Boolean {
        return if (stopped)
            false
        else countSemaphore.tryAcquire() && semaphore.tryAcquire()
    }

    fun stop() {
        stopped = true
        jobCounter.cancel()
    }
}