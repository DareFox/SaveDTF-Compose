package ui.composables

import androidx.compose.animation.*
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import models.Entry
import ui.SaveDtfTheme

@Preview
@Composable
fun EntryListPreview() {
    SaveDtfTheme(true) {
        EntryList(
            listOf(
                Entry("tes", "ets", true)
            )
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun EntryList(entries: List<Entry>) {
    Surface(color = Color.Black.copy(0.0f)) {
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

                println("column init")

                LazyColumn(state = lazyListState) {
                    items(entries, key = { it.id }) { entry ->
                        var state by rememberSaveable {
                            mutableStateOf(MutableTransitionState(false).apply {
                                targetState = true
                            })
                        }

                        AnimatedVisibility(
                            visibleState = state,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Box(modifier = Modifier.padding(bottom = 30.dp)) {
                                EntryCard(entry)
                            }
                        }
                    }
                }

                scope.launch {
                    lazyListState.animateScrollToItem(0)
                    lazyListState.animateScrollToItem(0) // scroll works only on second call, idk why ¯\_(ツ)_/¯
                }
            }

        }


    }
}