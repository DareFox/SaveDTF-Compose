package ui.menus

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import compose.icons.FeatherIcons
import compose.icons.feathericons.*
import ui.composables.CheckVersion
import ui.composables.FancyButton
import ui.composables.directoryDialog
import ui.i18n.AvailableLanguages
import ui.i18n.Lang
import ui.theme.CustomPallet
import ui.viewmodel.SettingsViewModel
import util.desktop.openUrl

@Composable
fun SettingsMenu() {
    val categories = mutableListOf<@Composable () -> Unit>()
    val lang by Lang.collectAsState()

    categories += {
        SettingsCategory(lang.settingsCategoryApp) {
            val fields = mutableListOf<@Composable () -> Unit>()

            val folderInput by SettingsViewModel.folderToSave.collectAsState()

            fields += {
                var clicks by remember { mutableStateOf(0) }
                var success by remember { mutableStateOf<Boolean?>(null) }
                var isCleared by remember { mutableStateOf(false) }

                val first = lang.settingsAppResetSettings
                val second = lang.settingsAppResetSettingsConfirmation

                val text = when {
                    clicks <= 0 -> first
                    clicks == 1 -> second
                    else -> if (success!!) lang.settingsAppResetSettingsSuccess else lang.settingsAppResetSettingsError
                }

                val colors = when(success) {
                    true -> ButtonDefaults.buttonColors(CustomPallet.successVariant)
                    false -> ButtonDefaults.buttonColors(CustomPallet.error)
                    else -> ButtonDefaults.buttonColors()
                }

                FancyButton(true, onClick = {
                    clicks++

                    if (clicks > 1 && !isCleared) {
                        success = SettingsViewModel.resetAllSettings()
                        isCleared = true // make reset button available only once
                    }
                }, placeholderButton = text, buttonColors = colors)
            }

            fields += {
                SettingsTextField(
                    name = lang.settingsAppSaveFolder,
                    input = folderInput ?: "",
                    textPlaceholder = lang.settingsAppSaveFolderPlaceholder,
                    onFieldClick = {
                        directoryDialog(lang.settingsAppNativeMenuDialog) {
                            if (it != null) {
                                SettingsViewModel.setFolderToSave(it)
                            }
                        }
                    }
                ) {}
            }

            fields += {
                val ignoreUpdates by SettingsViewModel.ignoreUpdate.collectAsState()

                SettingsBoolField(lang.settingsAppIgnoreUpdatesQuestion, ignoreUpdates) {
                    SettingsViewModel.setIgnoreUpdate(it)
                }
            }

            fields += {
                var showWindow by remember { mutableStateOf(false) }

                FancyButton(
                    enabled = true,
                    onClick = {
                        showWindow = !showWindow
                    },
                    buttonColors = ButtonDefaults.buttonColors(),
                    placeholderButton = lang.settingsAppCheckUpdates
                )

                if (showWindow) {
                    CheckVersion(true)
                }
            }

            fields += {
                val currentLanguage by SettingsViewModel.proxyLocale.collectAsState()

                SettingsDropdown("${currentLanguage.localeName} (${currentLanguage.localeTag})",
                    AvailableLanguages.map {
                    "${it.localeName} (${it.localeTag})" to it
                }) {
                    SettingsViewModel.setLocale(it)
                }
            }

            SettingsFields(fields)
        }
    }

    categories += {
        SettingsCategory(lang.settingsCategoryTokens) {
            val tokens by SettingsViewModel.tokens.collectAsState()
            val fields = mutableListOf<@Composable () -> Unit>()

            fields += {
                Text(
                    text = lang.settingsTokensHowToGet,
                    textDecoration = TextDecoration.Underline,
                    style = MaterialTheme.typography.subtitle1,
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier.clickable {
                        openUrl(lang.settingsTokensHowToGetURL)
                    },
                )
            }

            tokens.forEach {
                fields += {
                    val token = it.value ?: ""

                    SettingsTextField(
                        name = it.key.name,
                        input = token,
                        hideContent = true,
                        textPlaceholder = lang.settingsTokensPlaceholder
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
        SettingsCategory(lang.settingsCategoryDownload) {
            val textWidth = 400.dp
            var retryAmount by remember { mutableStateOf("${SettingsViewModel.retryAmount.value}") }
            var mediaTimeout by remember { mutableStateOf("${SettingsViewModel.mediaTimeoutInSeconds.value}") }
            var entryTimeout by remember { mutableStateOf("${SettingsViewModel.entryTimeoutInSeconds.value}") }

            val fields = mutableListOf<@Composable () -> Unit>()

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
                    name = lang.settingsDownloadMediaAttempts,
                    input = retryAmount,
                    textPlaceholder = lang.settingsDownloadMediaAttemptsPlaceholder,
                    hideContent = false,
                    width = textWidth
                ) {
                    it.toIntOrNull()?.also { retry ->
                        SettingsViewModel.setRetryAmount(retry)
                    }
                    retryAmount = it
                }
            }

            fields += {
                SettingsTextField(
                    name = lang.settingsDownloadMediaTimeout,
                    input = mediaTimeout,
                    textPlaceholder = lang.settingsDownloadMediaTimeoutPlaceholder,
                    hideContent = false,
                    width = textWidth
                ) {
                    it.toIntOrNull()?.also { timeout ->
                        SettingsViewModel.setMediaTimeoutInSeconds(timeout)
                    }
                    mediaTimeout = it
                }
            }

            // TODO: Implement usage for entry timeout setting
//            fields += {
//                SettingsTextField(
//                    name = "Время ожидание загрузки статьи",
//                    input = entryTimeout,
//                    textPlaceholder = "0 или отрицательное число секунд - бесконечное ожидание.",
//                    hideContent = false,
//                    width = textWidth
//                ) {
//                    it.toIntOrNull()?.also { timeout ->
//                        SettingsViewModel.setEntryTimeoutInSeconds(timeout)
//                    }
//                    entryTimeout = it
//                }
//            }


            val replaceErrors by SettingsViewModel.replaceErrorMedia.collectAsState()

            fields += {
                SettingsBoolField(lang.settingsDownloadMediaReplaceOnError, replaceErrors) {
                    SettingsViewModel.setReplaceErrorMedia(it)
                }
            }

            val downloadVideo by SettingsViewModel.downloadVideo.collectAsState()

            fields += {
                SettingsBoolField(lang.settingsDownloadVideoQuestion, downloadVideo) {
                    SettingsViewModel.setDownloadVideoMode(it)
                }
            }

            val downloadImage by SettingsViewModel.downloadImage.collectAsState()

            fields += {
                SettingsBoolField(lang.settingsDownloadImageQuestion, downloadImage) {
                    SettingsViewModel.setDownloadImageMode(it)
                }
            }

            SettingsFields(fields)
        }
    }

    categories += {
        SettingsCategory(lang.settingsCategoryCache) {
            var result by remember { mutableStateOf<Boolean?>(null) }
            val backgroundColor = animateColorAsState(
                when (result) {
                    null -> MaterialTheme.colors.primary
                    result as Boolean -> CustomPallet.successVariant
                    else -> Color.Red
                }
            )
            val contentColor = animateColorAsState(
                when (result) {
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
                placeholderButton = lang.settingsCacheClearAll
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
        Spacer(modifier = Modifier.fillMaxWidth().height(15.dp))
    }
}

@Composable
fun SettingsCategory(name: String, composable: @Composable () -> Unit) {
    Text(name, style = MaterialTheme.typography.h4)
    Spacer(modifier = Modifier.height(5.dp).fillMaxWidth())
    Divider(
        color = MaterialTheme.colors.primary,
        thickness = 2.dp,
        modifier = Modifier.clip(RoundedCornerShape(25))
    )
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
    width: Dp = 230.dp,
    onInputChange: (String) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(width)
        ) {
            Text(name, style = MaterialTheme.typography.subtitle1)
        }
        Box(modifier = Modifier.height(55.dp)) {
            var passwordVisible by remember { mutableStateOf(false) }

            val visualTransformation = if (hideContent && !passwordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            }

            val lang by Lang.collectAsState()

            OutlinedTextField(
                enabled = onFieldClick == null,
                value = input,
                onValueChange = onInputChange,
                textStyle = MaterialTheme.typography.subtitle1.copy(fontSize = 1.2.em),
                placeholder = {
                    Text(textPlaceholder, style = MaterialTheme.typography.subtitle1.copy(fontSize = 1.3.em))
                },
                modifier = Modifier.fillMaxWidth().clickable(onFieldClick != null) { onFieldClick?.invoke() },
                colors = TextFieldDefaults.outlinedTextFieldColors(disabledTextColor = MaterialTheme.colors.onBackground),
                visualTransformation = visualTransformation,
                trailingIcon = {
                    if (hideContent) {
                        val icon = if (passwordVisible) FeatherIcons.Eye else FeatherIcons.EyeOff

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(icon, lang.settingsTokensEyeIconDescription)
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
    onInputChange: (Boolean) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(name, style = MaterialTheme.typography.subtitle1)
        }
        Box(modifier = Modifier.requiredWidth(100.dp).scale(1.2f)) {
            Switch(
                input, onCheckedChange = onInputChange, colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colors.primary,
                    checkedTrackColor = MaterialTheme.colors.primaryVariant
                )
            )
        }
    }
}

@Composable
fun <T> SettingsDropdown(
    value: String,
    items: List<Pair<String, T>>,
    onFieldClick: (T) -> Unit
) {
    val lazyListState = rememberLazyListState()
    val shape = RoundedCornerShape(5.dp)
    var show by remember { mutableStateOf(false) }
    val icon = if (show) FeatherIcons.ChevronUp else FeatherIcons.ChevronDown

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .clip(shape)
                .border(1.dp, Color.Gray, shape)
                .clickable {
                show = !show
            },
            contentAlignment = Alignment.CenterStart
        ) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)) {
                Text(text = value, style = MaterialTheme.typography.subtitle1)
                Icon(icon, null)
            }
        }

        if (show) {
            LazyColumn(state = lazyListState, modifier = Modifier.heightIn(40.dp, 300.dp).clip(shape)) {
                items(items, key = { it.first }) {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clickable {
                            onFieldClick(it.second)
                            show = !show
                       }, contentAlignment = Alignment.CenterStart) {
                        Text(text = it.first, style = MaterialTheme.typography.subtitle1, modifier = Modifier.padding(start = 10.dp))
                    }
                }
            }
        }
    }


}