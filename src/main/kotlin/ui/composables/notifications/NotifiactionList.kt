package ui.composables.notifications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import viewmodel.NotificationsViewModel

@Composable
inline fun NotificationList() {
    val notifications by NotificationsViewModel.queue.collectAsState()
    val scope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    Column(horizontalAlignment = Alignment.CenterHorizontally){
        Spacer(modifier = Modifier.fillMaxWidth().height(10.dp))

        LazyColumn(state = lazyListState) {
            items(notifications.toList(), key = { it }) { notifiaction ->
                // Hoisting transition state to view model
                val creationStateMap by NotificationsViewModel.creationStateMap.collectAsState()
                val viewModelTransitionState = creationStateMap.getOrDefault(notifiaction, null)
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
                    NotificationsViewModel.setCreationState(transitionState, notifiaction)
                }

                val state by remember { mutableState }

                if (!state.targetState && !state.currentState) { // on animation EXIT end
                    NotificationsViewModel.remove(notifiaction)
                }

                AnimatedVisibility(
                    visibleState = state,
                    enter = expandVertically(),
                    exit = shrinkVertically(),
                ) {
                    Box(modifier = Modifier.padding(bottom = 30.dp)) {
                        SimpleNotification(notifiaction) {
                            state.targetState = false
                        }
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        if (notifications.isNotEmpty()) {
            VerticalScrollbar(
                modifier = Modifier.fillMaxHeight().align(Alignment.CenterEnd),
                adapter = rememberScrollbarAdapter(lazyListState)
            )
        }
    }

    scope.launch { // scroll on top, when adding new elements
        if (notifications.isNotEmpty()) {
            lazyListState.animateScrollToItem(0)
            lazyListState.animateScrollToItem(0) // scroll works only on second call, idk why ¯\_(ツ)_/¯
        }
    }
}