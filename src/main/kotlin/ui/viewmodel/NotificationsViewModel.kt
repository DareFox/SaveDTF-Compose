package ui.viewmodel

import androidx.compose.animation.core.MutableTransitionState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

object NotificationsViewModel {
    private val _queue = MutableStateFlow(setOf<NotificationData>())
    val queue: StateFlow<Set<NotificationData>> = _queue

    private val _creationStateMap = MutableStateFlow(mapOf<NotificationData, MutableTransitionState<Boolean>>())
    val creationStateMap: StateFlow<Map<NotificationData, MutableTransitionState<Boolean>>> = _creationStateMap

    private val autoRemoverMap = mutableMapOf<NotificationData, Job>()
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    fun add(notification: NotificationData) {
        _queue.update {
            if (!_queue.value.contains(notification)) {
                logger.info { "Adding $notification notification to queue" }
                autoRemoverMap[notification] = startRemoverJob(notification)

                /** auto create transition state **/
                setCreationState(MutableTransitionState(false).apply {
                    targetState = true
                }, notification)

                it + notification
            } else {
                it
            }
        }


    }

    fun remove(notification: NotificationData) {
        _queue.update {
            logger.info { "Removing $notification notification from queue" }
            it - notification
        }

        _creationStateMap.update {
            logger.debug { "Removing transition state for $notification" }
            it - notification
        }

        logger.debug { "Cancel auto remover job" }
        autoRemoverMap[notification]?.cancel()
    }

    fun clearAll() {
        _queue.value.forEach {
            remove(it)
        }
    }

    fun setCreationState(state: MutableTransitionState<Boolean>, notification: NotificationData) {
        _creationStateMap.update {
            val mutable = it.toMutableMap()
            mutable[notification] = state
            mutable
        }
    }

    private fun startRemoverJob(notification: NotificationData): Job {
        return coroutineScope.launch(CoroutineName("Auto notification timeout remover")) {
            delay(notification.onScreenDuration * 1000L)

            val state = creationStateMap.value.getOrDefault(
                notification,
                MutableTransitionState(true)
            ).apply {
                targetState = false
            }

            setCreationState(state, notification)
            delay(3000L) // TODO: Change delay to state transition end
            remove(notification)
        }
    }
}

/**
 * Data class representing notification with text.
 * - [text][String] = Text to show
 * - [type][NotificationType] = Type of notification. Changes how it'll look on screen
 * - [onScreenDuration][Int] = How long notification should last on screen **in seconds**. Negative value means infinite time
 */
data class NotificationData(val text: String, val type: NotificationType, val onScreenDuration: Int)

enum class NotificationType {
    ERROR, INFO, SUCCESS
}