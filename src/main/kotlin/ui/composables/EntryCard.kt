package ui.composables

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import models.Entry
import ui.SaveDtfTheme

@Composable
@Preview
fun PreviewCard() {
    SaveDtfTheme(true) {
        EntryCard(Entry("Entry", "urlhere", true))
    }
}

@Composable
fun EntryCard(entry: Entry) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(85.dp)
            .shadow(8.dp),
        shape = RoundedCornerShape(topStartPercent = 10, bottomStartPercent = 10),
        color = MaterialTheme.colors.primary
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(start = 12.dp)) {
            Text(
                entry.name,
                style = MaterialTheme.typography.h2,
                color = MaterialTheme.colors.onPrimary,

            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                entry.url,
                style = MaterialTheme.typography.subtitle1,
                color = MaterialTheme.colors.onPrimary,
                fontStyle = FontStyle.Italic,
            )
        }

        Image(
            painter = painterResource("img/hehe.webp"),
            contentDescription = null,
            contentScale = ContentScale.FillHeight,
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(align = Alignment.TopEnd, unbounded = true) // Make box to not constrain oversize
                .size(150.dp) // Oversize card
                .graphicsLayer { alpha = 0.9999f } // Workaround to enable alpha compositing
                .drawWithContent {
                    val colors = listOf(
                        Color.Transparent,
                        Color.Black
                    )
                    drawContent()
                    drawRect(
                        brush = Brush.horizontalGradient(colors),
                        blendMode = BlendMode.DstIn
                    )
                }
        )

    }
}
