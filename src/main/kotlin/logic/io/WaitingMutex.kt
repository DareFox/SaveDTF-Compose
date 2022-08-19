package logic.io

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock

/**
 * Mutex, which limits amount of threads/coroutines to 1
 */
class WaitingMutex(private val scope: CoroutineScope) {
    // LIMIT TO ONLY 1 WAITING COROUTINE AND 1 RUNNING COROUTINE
    private val waitingSemaphore = Semaphore(2, 0)

    private val workingMutex = Mutex()

    /**
     * Run block of code
     *
     * If other block is running, then this function will wait
     * If some block of code waiting too, then this function will ignore your call
     */
    fun tryToRun(block: suspend () -> Unit) {
        if (waitingSemaphore.tryAcquire()) {
            scope.launch {
                workingMutex.withLock {
                    block()
                }
            }.also {
                it.invokeOnCompletion {
                    waitingSemaphore.release()
                }
            }
        }
    }
}