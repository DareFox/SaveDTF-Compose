package ui.viewmodel.queue

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import util.UrlUtil

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

    fun canCreateQueueElement(url: String): Boolean {
        return UrlUtil.isEntry(url) || UrlUtil.isBookmarkLink(url) || UrlUtil.isUserProfile(url)
    }

    fun createAndAddQueueElement(url: String): IQueueElementViewModel? {
        val element = when {
            UrlUtil.isUserProfile(url) -> TODO("todo UserProfileDownloader")
            UrlUtil.isEntry(url) -> EntryQueueElementViewModel(url)
            UrlUtil.isBookmarkLink(url) -> TODO("todo BookmarkDownloader")
            else -> null
        }

        element.also { if (it != null) this.add(it) }
        return element
    }
}