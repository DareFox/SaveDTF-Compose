package ui.composables.queue

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.Trash2
import kotlinx.coroutines.launch
import ui.viewmodel.queue.QueueViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun QueueList() {
    Surface(color = MaterialTheme.colors.background, modifier = Modifier.fillMaxHeight()) {
        val entries by QueueViewModel.queue.collectAsState()

        if (entries.isEmpty()) {
            EmptyQueueList()
        } else {
            val scope = rememberCoroutineScope()
            val lazyListState = rememberLazyListState()

            Column() {
                Spacer(modifier = Modifier.fillMaxWidth().height(10.dp))

                LazyColumn(state = lazyListState) {
                    items(entries.toList(), key = { it }) { entry ->
                        // Hoisting transition state to view model
                        val creationStateMap by QueueViewModel.creationStateMap.collectAsState()
                        val viewModelTransitionState = creationStateMap.getOrDefault(entry, null)
                        val mutableState: MutableState<MutableTransitionState<Boolean>>

                        // If there's no transition state, create it
                        // Else, use already existing one
                        if (viewModelTransitionState != null) {
                            mutableState = mutableStateOf(viewModelTransitionState)
                        } else {
                            val transitionState = MutableTransitionState(false).apply {
                                targetState = true
                            }
                            mutableState = mutableStateOf(transitionState)
                            QueueViewModel.setCreationState(transitionState, entry)
                        }

                        val state by remember { mutableState }

                        if (!state.targetState && !state.currentState) { // on animation EXIT end
                            QueueViewModel.remove(entry)
                        }

                        AnimatedVisibility(
                            visibleState = state,
                            enter = expandVertically(),
                            exit = shrinkVertically(),
                        ) {
                            Box(modifier = Modifier.padding(bottom = 30.dp)) {
                                val status by entry.status.collectAsState()

                                val buttons = mutableListOf(
                                    ActionBarElement(FeatherIcons.Trash2, "delete") {
                                        state.targetState = false
                                    },
                                )

                                QueueCard(entry, buttons)
                            }
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                VerticalScrollbar(
                    modifier = Modifier.fillMaxHeight().align(Alignment.CenterEnd),
                    adapter = rememberScrollbarAdapter(lazyListState)
                )
            }

            scope.launch { // scroll on top, when adding new elements
                if (entries.isNotEmpty()) {
                    lazyListState.animateScrollToItem(0)
                    lazyListState.animateScrollToItem(0) // scroll works only on second call, idk why ¯\_(ツ)_/¯
                }
            }
        }
    }
}