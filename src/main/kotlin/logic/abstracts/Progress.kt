package logic.abstracts

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.yield

interface IProgress {
    /**
     * Field that represents the progress of object
     */
    val progress: StateFlow<String?>
}

/**
 * Abstract interface that implements [IProgress] functionality
 */
abstract class AbstractProgress : IProgress {
    /**
     * Mutable state flow of progress
     */
    protected val mutableProgress = MutableStateFlow<String?>(null)
    override val progress: StateFlow<String?> = mutableProgress

    /**
     * Set progress
     */
    protected fun progress(status: String) {
        mutableProgress.value = status
    }

    /**
     *  Run block of code with setting progress and return result of evaluated block of code
     */
    protected fun <T> withProgress(status: String, block: (() -> T)): T {
        progress(status)
        return block()
    }

    /**
     *  Run suspending block of code with setting progress and return result of evaluated block of code
     */
    protected suspend fun <T> withProgressSuspend(status: String, block: suspend (() -> T)): T {
        progress(status)
        yield()
        return block()
    }

    /**
     * Clear current progress status to null
     */
    protected fun clearProgress() {
        mutableProgress.value = null
    }
}