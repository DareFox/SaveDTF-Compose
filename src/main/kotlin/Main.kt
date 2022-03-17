import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

@Composable
@Preview
fun infoBlockPreview() {
    SaveDtfTheme(true) {
        InfoBanner()
    }
}

@Composable
fun InfoBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colors.background

    ) {
        Row(
            modifier = Modifier.padding(20.dp, 23.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var counter by remember { mutableStateOf(0) }
            var infoText by remember { mutableStateOf("Забекапь все свои (и не только свои) статьи при помощи одной кнопки!") }
            Image(
                painter = painterResource("img/charlie.jpg"),
                contentDescription = "SaveDTF Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.Black)
                    .clickable {
                        counter++
                        if (counter >= 10) {
                            infoText = "П Р Е К Р А Т И" + " И".repeat(counter - 10) + " ＞﹏＜"
                        }
                    }
            )
            Spacer(Modifier.width(20.dp))
            Column {
                Text(
                    text = "SaveDTF",
                    style = MaterialTheme.typography.h2,
                    color = MaterialTheme.colors.onBackground,
                )
                Text(
                    text = infoText,
                    style = MaterialTheme.typography.subtitle1,
                    color = MaterialTheme.colors.onBackground,
                )
            }
        }
    }
}

@Composable
@Preview
fun MenuPreview() {
    SaveDtfTheme(true) {
        Menu()
    }
}

@Composable
fun Menu() {
    Surface(Modifier.fillMaxWidth()) {
        var entries by remember { mutableStateOf(mutableListOf<Entry>()) }
        Column {
            var pageURL by remember { mutableStateOf("") }
            TextField(
                value = pageURL,
                textStyle = MaterialTheme.typography.subtitle1,
                shape = RectangleShape,
                onValueChange = {
                    pageURL = it
                },
                singleLine = true,
                placeholder = {
                    Text(
                        text = "Ссылка на профиль или пост на DTF",
                        style = MaterialTheme.typography.subtitle1
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    pageURL = "cleared!"
                },
                content = {
                    Text("Добавить в очередь")
                },
                modifier = Modifier.fillMaxWidth().defaultMinSize(Dp.Unspecified, 50.dp),
                shape = RoundedCornerShape(0, 0, 40, 40)
            )

            if (entries.isEmpty()) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Text(
                        text = "\"Пусто... Должно быть это ветер\"",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.subtitle1,
                        modifier = Modifier.fillMaxWidth(),
                        fontStyle = FontStyle.Italic
                    )
                }
            } else {

            }
        }
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "SaveDTF!",
        icon = painterResource("img/hehe.webp"),
    ) {
        SaveDtfTheme(true) {
            Surface(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
                Column {
                    InfoBanner()
                    Menu()
                }
            }
        }
    }
}
