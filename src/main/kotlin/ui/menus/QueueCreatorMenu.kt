package ui.menus

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ui.SaveDtfTheme
import ui.composables.InputField
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