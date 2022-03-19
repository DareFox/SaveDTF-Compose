package ui.composables

import ui.SaveDtfTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.isTypedEvent
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import models.Entry

@Composable
@Preview
fun MenuPreview() {
    SaveDtfTheme(true) {
        Menu()
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Menu() {
    Surface(Modifier.fillMaxWidth()) {
        Column() {
            var entries by remember { mutableStateOf(mutableStateListOf<Entry>()) }
            var pageURL by remember { mutableStateOf("") }
            TextField(
                value = pageURL,
                textStyle = MaterialTheme.typography.subtitle1,
                shape = RectangleShape,
                onValueChange = {
                    pageURL = it  // IT WILL RECOMPOSE entries; TODO: fix unnecessary recomposition
                },
                singleLine = true,
                placeholder = {
                    Text(
                        text = "Ссылка на профиль или пост на DTF",
                        style = MaterialTheme.typography.subtitle1
                    )
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { FocusRequester.Default.requestFocus() }),
                colors = TextFieldDefaults.textFieldColors(backgroundColor = MaterialTheme.colors.background),
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colors.background).onKeyEvent {
                    if (it.key == Key.Enter && pageURL.isNotEmpty() && it.type == KeyEventType.KeyUp) {
                        entries.add(0, Entry("Page Name", pageURL, true))
                        pageURL = ""
                    }
                    true
                },
            )
            Button(
                onClick = {
                    if (pageURL.isNotEmpty()) {
                        entries.add(0, Entry("Page Name", pageURL, true))
                    }
                    pageURL = ""
                },
                content = {
                    Text("Добавить в очередь", style = MaterialTheme.typography.subtitle1, fontWeight = FontWeight.Bold)
                },
                modifier = Modifier.fillMaxWidth().defaultMinSize(Dp.Unspecified, 50.dp),
                shape = RoundedCornerShape(0, 0, 40, 40)
            )

            if (entries.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
            }

            EntryList(entries)
        }
    }
}
