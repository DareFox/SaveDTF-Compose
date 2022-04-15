package ui.viewmodel.queue

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

object QueueViewModel {
    private val _queue = MutableStateFlow(listOf<IQueueElementViewModel>())
    val queue: StateFlow<List<IQueueElementViewModel>> = _queue

    fun add(element: IQueueElementViewModel) {
        _queue.update {
            it + element
        }
    }

    fun remove(element: IQueueElementViewModel) {
        _queue.update {
            it - element
        }
    }

    fun clear() {
        _queue.update {
            listOf()
        }
    }
}