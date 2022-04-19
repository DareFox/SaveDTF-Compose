package ui.menus

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.Eye
import compose.icons.feathericons.EyeOff
import ui.composables.FancyButton
import ui.composables.directoryDialog
import ui.viewmodel.SettingsViewModel

@Composable
fun SettingsMenu() {
    val categories = mutableListOf<@Composable () -> Unit>()

    categories += {
        SettingsCategory("Токены") {
            val tokens by SettingsViewModel.tokens.collectAsState()
            val fields = mutableListOf<@Composable () -> Unit>()
            tokens.forEach {
                fields += {
                    val token = it.value ?: ""

                    SettingsTextField(
                        name = it.key.name,
                        input = token,
                        hideContent = true,
                        textPlaceholder = "Вставь токен сюда"
                    ) { input ->
                        if (input.isEmpty() || input.isBlank()) {
                            SettingsViewModel.setToken(null, it.key)
                        } else {
                            SettingsViewModel.setToken(input, it.key)
                        }
                    }
                }
            }

            SettingsFields(fields)
        }
    }


    categories += {
        SettingsCategory("Загрузка") {
            val folderInput by SettingsViewModel.folderToSave.collectAsState()
            val fields = mutableListOf<@Composable () -> Unit>()

            fields += {
                SettingsTextField(
                    name = "Папка",
                    input = folderInput ?: "",
                    textPlaceholder = "Нажми на меня, чтобы выбрать папк",
                    onFieldClick = {
                        directoryDialog("Choose Folder") {
                            if (it != null) {
                                SettingsViewModel.setFolderToSave(it)
                            }
                        }
                    }
                ) {}
            }

            var input by remember { mutableStateOf("${SettingsViewModel.retryAmount.value}") }

            fields += {
                /*
                    Not using ViewModel state as input because OutlinedTextField works strange.

                    e.g.:
                    input = 100,
                    user press -> "e" -> callback on input "100e" ->
                    callback will be ignored because it's not number and screen will not be changed

                    input = 100 (same)
                    user press -> "a" -> callback on input "100ea"(ignores current TextField input!!)  ->
                    callback will be ignored because it's not number and screen will not be changed

                    input = 100 (same)
                    user press -> 0 -> callback on input "100ea0" ->
                    callback will be ignored because it's not number and screen will not be changed

                    But from user it'll look strange. Because user assumes that input is "1000", not "100ea0".

                    To avoid this problem, just print user input on screen, but set new value only if it's number.
                    On recomposition, previous incorrect input will be wiped and TextInput will show current state of attempts
                 */
                SettingsTextField(
                    name = "Кол-во попыток",
                    input = input,
                    textPlaceholder = "0 — бесконечность, значение меньше нуля ",
                    hideContent = false
                ) {
                    it.toIntOrNull()?.also { retry ->
                        SettingsViewModel.setRetryAmount(retry)
                    }
                    input = it
                }
            }

            val replaceErrors by SettingsViewModel.replaceErrorMedia.collectAsState()

            fields += {
                SettingsBoolField("При ошибке сохранения медиа, заменять их на заглушку?", replaceErrors) {
                    SettingsViewModel.setReplaceErrorMedia(it)
                }
            }

            val downloadVideo by SettingsViewModel.downloadVideo.collectAsState()

            fields += {
                SettingsBoolField("Скачивать видео?", downloadVideo) {
                    SettingsViewModel.setDownloadVideoMode(it)
                }
            }

            val downloadImage by SettingsViewModel.downloadImage.collectAsState()

            fields += {
                SettingsBoolField("Скачивать изображения?", downloadImage) {
                    SettingsViewModel.setDownloadImageMode(it)
                }
            }

            SettingsFields(fields)
        }
    }

    categories += {
        SettingsCategory("Кэш") {
            var result by remember { mutableStateOf<Boolean?>(null) }
            val backgroundColor = animateColorAsState(
                when(result) {
                    null -> MaterialTheme.colors.primary
                    result as Boolean -> Color.Green.copy(0.5f)
                    else -> Color.Red
                }
            )
            val contentColor = animateColorAsState(
                when(result) {
                    null -> MaterialTheme.colors.onPrimary
                    result as Boolean -> Color.White
                    else -> Color.Black
                }
            )

            FancyButton(
                enabled = true,
                onClick = {
                    result = SettingsViewModel.clearCache()
                },
                buttonColors = ButtonDefaults.buttonColors(backgroundColor.value, contentColor.value),
                placeholderButton = "Очистить кэш"
            )
        }
    }

    val lazyListState = rememberLazyListState()

    Box {
        LazyColumn(modifier = Modifier.padding(25.dp, 0.dp), state = lazyListState) {
            items(categories) {
                it()
                Spacer(modifier = Modifier.fillMaxWidth().height(10.dp))
            }
        }
        VerticalScrollbar(
            modifier = Modifier.fillMaxHeight().align(Alignment.CenterEnd),
            adapter = rememberScrollbarAdapter(lazyListState)
        )
    }
}

@Composable
fun SettingsFields(field: List<@Composable () -> Unit>) {
    field.forEach {
        it()
        Spacer(modifier = Modifier.fillMaxWidth().height(5.dp))
    }
}

@Composable
fun SettingsCategory(name: String, composable: @Composable () -> Unit) {
    Text(name, style = MaterialTheme.typography.h4)
    Spacer(modifier = Modifier.height(5.dp).fillMaxWidth())
    Divider(color = MaterialTheme.colors.primary,
        thickness = 2.dp,
        modifier = Modifier.clip(RoundedCornerShape(25)))
    Spacer(modifier = Modifier.height(15.dp).fillMaxWidth())

    composable()
}

@Composable
fun SettingsTextField(
    name: String,
    input: String,
    textPlaceholder: String,
    hideContent: Boolean = false,
    onFieldClick: (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onInputChange: (String) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier
            .width(230.dp)
        ) {
            Text(name, style = MaterialTheme.typography.subtitle1)
        }
        Box() {
            var passwordVisible by remember { mutableStateOf(false) }

            val visualTransformation = if (hideContent && !passwordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            }

            OutlinedTextField(
                enabled = onFieldClick == null,
                value = input,
                onValueChange = onInputChange,
                textStyle = MaterialTheme.typography.subtitle2,
                placeholder = {
                    Text(textPlaceholder, style = MaterialTheme.typography.subtitle2)
                },
                modifier = Modifier.fillMaxWidth().clickable(onFieldClick != null) { onFieldClick?.invoke() },
                colors = TextFieldDefaults.outlinedTextFieldColors(disabledTextColor = MaterialTheme.colors.onBackground),
                visualTransformation = visualTransformation,
                trailingIcon = {
                    if (hideContent) {
                        val icon = if (passwordVisible) FeatherIcons.Eye else FeatherIcons.EyeOff

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(icon, "Hide/Show password")
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun SettingsBoolField(
    name: String,
    input: Boolean,
    onInputChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier
            .fillMaxWidth()
        ) {
            Text(name, style = MaterialTheme.typography.subtitle1)
        }
        Box(modifier = Modifier.requiredWidth(100.dp).scale(1.2f)) {
            Switch(input, onCheckedChange = onInputChange, colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colors.primary,
                checkedTrackColor = MaterialTheme.colors.primaryVariant
            ))
        }
    }
}