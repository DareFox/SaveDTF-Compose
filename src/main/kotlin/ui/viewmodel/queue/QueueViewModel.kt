package ui.viewmodel.queue

import androidx.compose.animation.core.MutableTransitionState
import exception.errorOnNull
import kmtt.models.enums.Website
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import ui.viewmodel.DebugQueueViewModel
import ui.viewmodel.SettingsViewModel
import util.kmttapi.UrlUtil

object QueueViewModel {
    private val _queue = MutableStateFlow(setOf<IQueueElementViewModel>())
    val queue: StateFlow<Set<IQueueElementViewModel>> = _queue

    private val _creationStateMap = MutableStateFlow(mapOf<IQueueElementViewModel, MutableTransitionState<Boolean>>())
    val creationStateMap: StateFlow<Map<IQueueElementViewModel, MutableTransitionState<Boolean>>> = _creationStateMap

    private val urlChecks: List<(String) -> Boolean> = listOf(
        UrlUtil::isEntry,
        UrlUtil::isBookmarkLink,
        { it == "debug" }
    )

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

    fun addBookmarksElement(website: Website) {
        val token = SettingsViewModel.tokens.value[Website.DTF].errorOnNull("$website token is null")
        add(BookmarksElementViewModel(website, token))
    }

    fun canCreateQueueElement(url: String): Boolean {
        return urlChecks.any { it(url) }
    }

    fun createAndAddQueueElement(url: String) {
        when {
            url == "debug" ->  DebugQueueViewModel.startQueue.forEach {
                add(it)
            }
//            UrlUtil.isUserProfile(url) -> TODO("todo UserProfileDownloader")
            UrlUtil.isEntry(url) -> add(EntryQueueElementViewModel(url))
            UrlUtil.isBookmarkLink(url) -> addBookmarksElement(UrlUtil.getWebsiteType(url)!!) // we do a little bit of trolling !!
        }

    }

    fun setCreationState(state: MutableTransitionState<Boolean>, element: IQueueElementViewModel) {
        _creationStateMap.update {
            val mutable = it.toMutableMap()
            mutable[element] = state
            mutable
        }
    }
}