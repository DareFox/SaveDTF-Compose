package ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
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
    placeholderButton: String,
    enabled: Boolean,
    isError: Boolean,
    errorMessage: String?
) {
    println(input)
    OutlinedTextField(
        value = input,
        textStyle = MaterialTheme.typography.subtitle2,
        shape = RoundedCornerShape(25),
        onValueChange = {
            onInputChange(it)
        },
        singleLine = true,
        placeholder = {
            Text(
                text = placeholderInput,
                style = MaterialTheme.typography.subtitle2
            )
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { FocusRequester.Default.requestFocus() }),
        colors = TextFieldDefaults.textFieldColors(backgroundColor = MaterialTheme.colors.background),
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colors.background).onKeyEvent {
            if (it.key == Key.Enter && it.type == KeyEventType.KeyUp && enabled) {
                onConfirm(input)
            }
            true
        },
        isError = isError,
    )

    if (isError && errorMessage != null) {
        Spacer(modifier = Modifier.fillMaxWidth().height(10.dp))
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.subtitle2,
            color = MaterialTheme.colors.onError,
            modifier = Modifier.padding(20.dp, 0.dp)
        )
    }

    Spacer(modifier = Modifier.fillMaxWidth().height(10.dp))
    Button(
        enabled = enabled,
        onClick = {
            onConfirm(input)
        },
        content = {
            Text(placeholderButton, style = MaterialTheme.typography.subtitle2, fontWeight = FontWeight.Bold)
        },
        modifier = Modifier.fillMaxWidth().defaultMinSize(Dp.Unspecified, 50.dp).clip(shape = RoundedCornerShape(25)),
    )
}