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
import util.SharedRegex

@Composable
@Preview
fun QueueCreatorMenuPreview() {
    SaveDtfTheme(true) {
        QueueCreatorMenu(listOf(), {}, {})
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun QueueCreatorMenu(entries: List<String>, addEntry: (String) -> Unit, entryRemove: (String) -> Unit) {
    Surface(Modifier.fillMaxWidth()) {
        Column() {
            var input by rememberSaveable { mutableStateOf("") }
            var enable by rememberSaveable { mutableStateOf(false) }

            val supported = SharedRegex.entryUrlRegex.find(input) != null
            val errorMessage = if(input.isNotEmpty() && !supported) "Неверный URL" else null
            enable = input.isNotEmpty() && supported

            InputField(
                onInputChange = {
                    input = it
                },
                onConfirm = {
                    addEntry(it)
                },
                input = input,
                placeholderInput = "Вставь ссылку сюда",
                placeholderButton = "Добавить в очередь",
                enabled = enable,
                isError = input.isNotEmpty() && !supported,
                errorMessage = errorMessage
             )

            QueueList(entries, entryRemove)
        }
    }
}
