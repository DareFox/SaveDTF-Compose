package ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun InputField(
    onInputChange: (String) -> Unit,
    onConfirm: (String) -> Unit,
    input: String,
    placeholderInput: String,
    placeholderButton: String
) {
    println(input)
    TextField(
        value = input,
        textStyle = MaterialTheme.typography.subtitle1,
        shape = RectangleShape,
        onValueChange = {
            onInputChange(it)
            // ???????? // IT WILL RECOMPOSE entries; TODO: fix unnecessary recomposition
        },
        singleLine = true,
        placeholder = {
            Text(
                text = placeholderInput,
                style = MaterialTheme.typography.subtitle1
            )
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { FocusRequester.Default.requestFocus() }),
        colors = TextFieldDefaults.textFieldColors(backgroundColor = MaterialTheme.colors.background),
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colors.background).onKeyEvent {
            if (it.key == Key.Enter && it.type == KeyEventType.KeyUp) {
                onConfirm(input)
            }
            true
        },
    )
    Button(
        onClick = {
            onConfirm(input)
        },
        content = {
            Text(placeholderButton, style = MaterialTheme.typography.subtitle1, fontWeight = FontWeight.Bold)
        },
        modifier = Modifier.fillMaxWidth().defaultMinSize(Dp.Unspecified, 50.dp),
    )
}