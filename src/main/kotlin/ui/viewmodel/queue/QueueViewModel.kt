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

    private val urlChecks: List<UrlChecker> = listOf(
        UrlChecker(UrlUtil::isEntry) {
            add(EntryQueueElementViewModel(it))
        },
        UrlChecker(UrlUtil::isBookmarkLink) {
            add(createBookmarks(UrlUtil.getWebsiteType(it)!!)) // we do a little bit of trolling !!
        },
        UrlChecker( { it == "debug"} ) {
            DebugQueueViewModel.startQueue.forEach {
                add(it)
            }
        }
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

    fun canCreateBookmarks(website: Website): Boolean {
        // check if token isn't empty
        return SettingsViewModel.tokens.value[website]?.isNotEmpty() ?: false
    }

    fun createBookmarks(website: Website): BookmarksElementViewModel {
        val token = SettingsViewModel.tokens.value[Website.DTF].errorOnNull("$website token is null")
        return BookmarksElementViewModel(website)
    }

    fun canCreateQueueElement(url: String): Boolean {
        return urlChecks.any { it.check(url) }
    }

    fun createAndAddQueueElement(url: String) {
        urlChecks.any {
            val check = it.check(url)
            if (check) {
                it.execute(url)
            }

            check
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

data class UrlChecker(
    /**
     * Can this url be executed by this checker?
     */
    val check: (String) -> Boolean,

    /**
     * Execute action to do add url to queue
     */
    val execute: (String) -> Unit
)