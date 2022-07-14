package ui.viewmodel.queue

import androidx.compose.animation.core.MutableTransitionState
import exception.errorOnNull
import kmtt.models.enums.Website
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import mu.KotlinLogging
import ui.viewmodel.DebugQueueViewModel
import ui.viewmodel.SettingsViewModel
import util.kmttapi.UrlUtil
import kotlin.math.log

private val logger = KotlinLogging.logger { }

object QueueViewModel {
    private val _queue = MutableStateFlow(setOf<IQueueElementViewModel>())
    val queue: StateFlow<Set<IQueueElementViewModel>> = _queue

    private val _creationStateMap = MutableStateFlow(mapOf<IQueueElementViewModel, MutableTransitionState<Boolean>>())
    val creationStateMap: StateFlow<Map<IQueueElementViewModel, MutableTransitionState<Boolean>>> = _creationStateMap

    private val urlChecks: List<UrlChecker> = listOf(
        UrlChecker(UrlUtil::isUserProfile) {
            add(ProfileElementViewModel(
                UrlUtil.getWebsiteType(it)!!,
                UrlUtil.getProfileID(it)
            ))
        },
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
            logger.info { "Adding $element to queue" }
            it + element
        }
    }

    fun remove(element: IQueueElementViewModel) {
        _queue.update {
            logger.info { "Removing $element from queue" }
            it - element
        }

        // Remove transition state
        _creationStateMap.update {
            logger.debug { "Removing transition state of creation" }
            it - element
        }
    }

    fun clear() {
        logger.info { "Clearing queue" }
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
            logger.debug { "Setting creation state of $element element to [current = ${state.currentState}; target = ${state.targetState}]" }
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