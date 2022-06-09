package ui.viewmodel.queue

import androidx.compose.animation.core.MutableTransitionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import util.kmttapi.UrlUtil

object QueueViewModel {
    private val _queue = MutableStateFlow(setOf<IQueueElementViewModel>())
    val queue: StateFlow<Set<IQueueElementViewModel>> = _queue

    private val _creationStateMap = MutableStateFlow(mapOf<IQueueElementViewModel, MutableTransitionState<Boolean>>())
    val creationStateMap: StateFlow<Map<IQueueElementViewModel, MutableTransitionState<Boolean>>> = _creationStateMap

    fun add(element: IQueueElementViewModel) {
        _queue.update {
            it + element
        }
    }

    fun remove(element: IQueueElementViewModel) {
        _queue.update {
            it - element
        }

        // Remove transition state
        _creationStateMap.update {
            it - element
        }
    }

    fun clear() {
        _queue.update {
            setOf()
        }
    }

    fun canCreateQueueElement(url: String): Boolean {
        return UrlUtil.isEntry(url)
    }

    fun createAndAddQueueElement(url: String): IQueueElementViewModel? {
        val element = when {
//            UrlUtil.isUserProfile(url) -> TODO("todo UserProfileDownloader")
            UrlUtil.isEntry(url) -> EntryQueueElementViewModel(url)
//            UrlUtil.isBookmarkLink(url) -> TODO("todo BookmarkDownloader")
            else -> null
        }

        element.also { if (it != null) add(it) }
        return element
    }

    fun setCreationState(state: MutableTransitionState<Boolean>, element: IQueueElementViewModel) {
        _creationStateMap.update {
            val mutable = it.toMutableMap()
            mutable[element] = state
            mutable
        }
    }
}