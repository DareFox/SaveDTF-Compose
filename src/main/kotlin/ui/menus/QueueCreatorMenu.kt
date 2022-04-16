package ui.menus

import ui.SaveDtfTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import ui.composables.QueueList
import ui.composables.InputField
import ui.viewmodel.queue.QueueViewModel
import util.SharedRegex
import util.UrlUtil

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

            InputField(
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

            QueueList()
        }
    }
}