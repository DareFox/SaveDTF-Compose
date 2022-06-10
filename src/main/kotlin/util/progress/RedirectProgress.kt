package util.progress

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
    return this.progress.onEach { update ->
       to.value = transform(update)
    }.launchIn(scope)
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
    return this.onEach { update ->
        to.value = transform(update)
    }.launchIn(scope)
}