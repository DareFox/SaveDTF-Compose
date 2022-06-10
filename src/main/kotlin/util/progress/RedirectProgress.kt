package util.progress

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import logic.abstracts.IProgress

/**
 * Copy progress from [IProgress] to other [MutableStateFlow]
 *
 * @return [Job] that runs redirection mechanism
 */
fun IProgress.redirectTo(
    to: MutableStateFlow<String?>,
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
): Job {
    return this.redirectTo(to, scope) { it }
}

/**
 * Copy & transform progress from [IProgress] to other [MutableStateFlow]
 *
 * @return [Job] that runs redirection mechanism
 */
fun <T> IProgress.redirectTo(
    to: MutableStateFlow<T>,
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    transform: (String?) -> T
): Job {
    val flow = this.progress.onEach { update ->
        yield()
        to.value = transform(update)
    }

    return scope.launch(CoroutineName("StateFlow Redirection")) {
        flow.collect()
    }
}

/**
 * Copy state from [StateFlow] to another [StateFlow]
 *
 * @return [Job] that runs redirection mechanism
 */
fun <T> StateFlow<T>.redirectTo(
    to: MutableStateFlow<T>,
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
): Job {
    return this.redirectTo(to, scope) { it }
}

/**
 * Copy & transform state from [StateFlow] to another [StateFlow]
 *
 * @return [Job] that runs redirection mechanism
 */
fun <T, X> StateFlow<T>.redirectTo(
    to: MutableStateFlow<X>,
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    transform: (T) -> X
): Job {
    val flow = this.onEach { update ->
        yield()
        to.value = transform(update)
    }

    return scope.launch(CoroutineName("StateFlow Redirection")) {
        flow.collect()
    }
}