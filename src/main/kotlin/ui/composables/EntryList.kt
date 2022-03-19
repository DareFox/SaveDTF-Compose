package ui.composables

import androidx.compose.animation.*
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.Trash2
import kotlinx.coroutines.launch
import models.ActionBarElement
import models.Entry
import ui.SaveDtfTheme

@Preview
@Composable
fun EntryListPreview() { // no Preview becuase animation can't load in this mode
    SaveDtfTheme(true) {
        EntryList(
            mutableListOf(
                Entry("tes", "ets", true)
            )
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun EntryList(entries: MutableList<Entry>) {
    Surface(color = MaterialTheme.colors.background, modifier = Modifier.fillMaxHeight()) {
        Crossfade(entries) {
            if (entries.isEmpty()) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Text(
                        text = "\"Пусто... Должно быть это ветер\"",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.subtitle1,
                        modifier = Modifier.fillMaxWidth(),
                        fontStyle = FontStyle.Italic
                    )
                }
            } else {
                val scope = rememberCoroutineScope()
                val lazyListState = rememberLazyListState()

                LazyColumn(state = lazyListState) {
                    items(entries, key = { it.id }) { entry ->
                        var state by rememberSaveable {
                            mutableStateOf(MutableTransitionState(false).apply {
                                targetState = true
                            })
                        }

                        if (!state.targetState && !state.currentState) { // on animation EXIT end
                            println("deleted")
                            entries.remove(entry)
                        }

                        AnimatedVisibility(
                            visibleState = state,
                            enter = expandVertically(),
                            exit = shrinkVertically(),
                        ) {
                            println(entry)
                            Box(modifier = Modifier.padding(bottom = 30.dp)) {
                                EntryCard(entry, listOf(ActionBarElement(FeatherIcons.Trash2, "delete") {
                                    state.targetState = false
                                }))
                            }
                        }
                    }
                }

                scope.launch { // scroll on top, when adding new elements
                    lazyListState.animateScrollToItem(0)
                    lazyListState.animateScrollToItem(0) // scroll works only on second call, idk why ¯\_(ツ)_/¯
                }
            }

        }


    }
}