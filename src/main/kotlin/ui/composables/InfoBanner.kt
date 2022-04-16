package ui.composables

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ui.SaveDtfTheme
import util.repeatOrEmpty

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
                    .shadow(10.dp, CircleShape, false)
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.Black)
                    .clickable {
                        counter++
                        if (counter >= 10) {
                            infoText = "П Р Е К Р А Т И" + " И".repeatOrEmpty(counter - 10) + " ＞﹏＜"
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
                    style = MaterialTheme.typography.subtitle2,
                    color = MaterialTheme.colors.onBackground,
                )
            }
        }
    }
}
