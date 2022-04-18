package ui.menus

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.Download
import ui.SaveDtfTheme
import ui.composables.FancyButton
import ui.composables.FancyInputField
import ui.composables.queue.QueueList
import ui.viewmodel.queue.QueueViewModel

@Composable
@Preview
fun QueueCreatorMenuPreview() {
    SaveDtfTheme(true) {
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun QueueCreatorMenu() {
    Surface(Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(10.dp, 0.dp)) {
            val viewmodel = QueueViewModel

            var input by rememberSaveable { mutableStateOf("") }
            var enable by rememberSaveable { mutableStateOf(false) }


            val supported = viewmodel.canCreateQueueElement(input)
            val errorMessage = if(input.isNotEmpty() && !supported) "Неверный URL" else null

            enable = input.isNotEmpty() && supported

            FancyInputField(
                onInputChange = {
                    input = it
                },
                onConfirm = {
                    viewmodel.createAndAddQueueElement(it)
                },
                input = input,
                placeholderInput = "Вставь ссылку сюда",
                placeholderButton = "Добавить в очередь",
                enabled = enable,
                isError = input.isNotEmpty() && !supported,
                errorMessage = errorMessage
             )
            Spacer(modifier = Modifier.fillMaxWidth().height(10.dp))
            FancyButton(true, onClick = {
                viewmodel.createAndAddQueueElement(input)
            }, "Добавить в очередь")

            QueueList()
        }
    }
}