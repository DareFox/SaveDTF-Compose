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
import me.darefox.saveDTF_compose.BuildConfig
import ui.SaveDtfTheme
import ui.i18n.Lang
import util.string.repeatOrEmpty
import viewmodel.AppViewModel

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
        val langState by Lang.collectAsState()
        Row(
            modifier = Modifier.padding(20.dp, 23.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var counter by remember { mutableStateOf(0) }

            val infoText = if (counter - 10 > 0) {
                val char = " " + langState.infoBannerClickSpamChar
                langState.infoBannerClickSpam.format(char.repeatOrEmpty(counter - 10))
            } else {
                langState.infoBanner
            }

            Image(
                painter = painterResource("img/charlie_95px.png"),
                contentDescription = "SaveDTF Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .shadow(10.dp, CircleShape, false)
                    .size(95.dp)
                    .clip(CircleShape)
                    .background(Color.Black)
                    .clickable {
                        counter++
                    }
            )
            Spacer(Modifier.width(20.dp))
            Column {
                Text(
                    text = "SaveDTF ${AppViewModel.currentVersionObject} ${if (BuildConfig.IS_DEV_VERSION) "(${BuildConfig.APP_BUILD_NUMBER}) β" else ""}",
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
