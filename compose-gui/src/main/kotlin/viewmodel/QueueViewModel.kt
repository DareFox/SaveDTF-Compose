package viewmodel.queue

import Downloader
import androidx.compose.animation.core.MutableTransitionState
import exception.errorOnNull
import kmtt.models.enums.Website
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import mu.KotlinLogging
import shared.saveable.BookmarksSaveable
import shared.saveable.ISaveable
import shared.util.kmttapi.KmttRegex
import shared.util.kmttapi.KmttUrl
import shared.util.kmttapi.KmttUrl.getWebsiteType
import viewmodel.DebugQueueViewModel
import viewmodel.SettingsViewModel

private val logger = KotlinLogging.logger { }

object QueueViewModel {
    private val _queue = MutableStateFlow(setOf<ISaveable>())
    val queue: StateFlow<Set<ISaveable>> = _queue

    private val _creationStateMap = MutableStateFlow(mapOf<ISaveable, MutableTransitionState<Boolean>>())
    val creationStateMap: StateFlow<Map<ISaveable, MutableTransitionState<Boolean>>> = _creationStateMap

    private val urlChecks: List<UrlChecker> = listOf(
        UrlChecker(KmttUrl::isPeriodSitemap) {
            add(Downloader.periodEntries(it, getWebsiteType(it)!!))
        },
        UrlChecker(KmttUrl::isUserProfile) {
            add(
                Downloader.profile(
                    getWebsiteType(it)!!,
                    KmttUrl.getProfileID(it)
                )
            )
        },
        UrlChecker(KmttUrl::isEntry) check@{
            // url should start from https to get entry from API
            // todo: maybe don't call same regex twice?
            val url = "https://" + (KmttRegex.entryUrlRegex.find(it)?.value ?: return@check)
            add(Downloader.entry(url))
        },
        UrlChecker(KmttUrl::isBookmarkLink) {
            add(createBookmarks(getWebsiteType(it)!!)) // we do a little bit of trolling !!
        },
        UrlChecker({ it == "debug" }) {
            DebugQueueViewModel.startQueue.forEach {
                add(it)
            }
        },
        UrlChecker(KmttUrl::isSitemapAll) {
            add(Downloader.allEntries(getWebsiteType(it)!!))
        },
        UrlChecker(KmttUrl::isEmptyWebsite) {
            add(Downloader.allEntries(getWebsiteType(it)!!))
        }
    )

    fun add(element: ISaveable) {
        _queue.update {
            logger.info { "Adding $element to queue" }
            it + element
        }
    }

    fun remove(element: ISaveable) {
        _queue.update {
            logger.info { "Removing $element from queue" }
            element.currentJob.value?.cancel()
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
        _queue.update { queueList ->
            queueList.forEach { it.currentJob.value?.cancel() }
            setOf()
        }
    }

    fun canCreateBookmarks(website: Website): Boolean {
        // check if token isn't empty
        return SettingsViewModel.tokens.value[website]?.isNotEmpty() ?: false
    }

    fun createBookmarks(website: Website): BookmarksSaveable {
        val token = SettingsViewModel.tokens.value[Website.DTF].errorOnNull("$website token is null")
        return Downloader.bookmarks(website, token)
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

    fun setCreationState(state: MutableTransitionState<Boolean>, element: ISaveable) {
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