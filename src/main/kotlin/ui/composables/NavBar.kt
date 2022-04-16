package ui.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ui.SaveDtfTheme

@Preview
@Composable
fun NavBarPreview() {
    SaveDtfTheme {
        var position by remember { mutableStateOf(0) }

        TextNavBar(listOf(
            TextBarElement("Token") { position = it},
            TextBarElement("Test") { position = it},
            TextBarElement("I forgor") { position = it}
        ), position)

    }
}

@Composable
fun NavBar(elements: List<NavBarElement>, selectedIndex: Int) {
    Surface(
        modifier = Modifier.requiredHeight(70.dp).fillMaxWidth(),
        color = MaterialTheme.colors.background
    ) {
        Row(horizontalArrangement = Arrangement.SpaceEvenly) {
            for ((index, element) in elements.withIndex()) {
                val state by rememberSaveable {
                    mutableStateOf(MutableTransitionState(false))
                }
                state.targetState = selectedIndex == index
                Surface(modifier = Modifier.weight(1f).clickable { element.onClick(index) }) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        element.composable(state, index)
                    }
                    AnimatedVisibility(state) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                            Divider(color = MaterialTheme.colors.primary, thickness = 4.dp)
                        }
                    }
                }
            }
        }
    }
}

data class NavBarElement(
    val onClick: (Int) -> Unit,
    val composable: @Composable (MutableTransitionState<Boolean>, Int) -> Unit
)

data class IconBarElement(val icon: ImageVector, val onClick: (Int) -> Unit)

@Composable
fun IconNavBar(elements: List<IconBarElement>, selectedIndex: Int) {
    val fancyElements = elements.map {
        NavBarElement(it.onClick) { state, index ->
            Box {
                Icon(it.icon, null, tint = MaterialTheme.colors.onBackground)
                Column {
                    AnimatedVisibility(state, enter = fadeIn(), exit = fadeOut()) {
                        Icon(it.icon, null, tint = MaterialTheme.colors.primary)
                    }
                }
            }
        }
    }

    NavBar(fancyElements, selectedIndex)
}

data class IconTextBarElement(val icon: ImageVector, val name: String, val onClick: (Int) -> Unit)

@Composable
fun IconTextNavBar(elements: List<IconTextBarElement>, selectedIndex: Int) {
    val fancyElements = elements.map {
        NavBarElement(it.onClick) { state, index ->
            Box {
                Icon(it.icon, null, tint = MaterialTheme.colors.onBackground)
                Column {
                    AnimatedVisibility(state, enter = fadeIn(), exit = fadeOut()) {
                        Icon(it.icon, null, tint = MaterialTheme.colors.primary)
                    }
                }
            }
            Text(
                text = it.name,
                style = MaterialTheme.typography.subtitle2
            )
        }
    }

    NavBar(fancyElements, selectedIndex)
}

data class TextBarElement(val name: String, val onClick: (Int) -> Unit)

@Composable
fun TextNavBar(elements: List<TextBarElement>, selectedIndex: Int) {
    val fancyElements = elements.map {
        NavBarElement(it.onClick) { state, index ->
            Box {
                Text(
                    text = it.name,
                    style = MaterialTheme.typography.subtitle2,
                    color = MaterialTheme.colors.onBackground
                )
                Column {
                    AnimatedVisibility(state, enter = fadeIn(), exit = fadeOut()) {
                        Text(
                            text = it.name,
                            style = MaterialTheme.typography.subtitle2,
                            color = MaterialTheme.colors.primary
                        )
                    }
                }
            }

        }
    }

    NavBar(fancyElements, selectedIndex)
}