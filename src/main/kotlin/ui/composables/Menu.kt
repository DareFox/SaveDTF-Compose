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
            var input by remember { mutableStateOf("") }

            InputField(
                onInputChange = {
                    println("oninputchange " + it)
                    pageURL = it
                    input = it
                },
                onConfirm = {
                    println("onConfirm " + it)
                    pageURL = it;
                    input = ""
                },
                input = input,
                placeholderInput = "input placeholder",
                placeholderButton = "button placeholder"
            )

            if (entries.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
            }

            EntryList(entries)
        }
    }
}
