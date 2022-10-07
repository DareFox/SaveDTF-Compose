package ui.composables.queue

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import shared.i18n.Lang

@Composable
fun EmptyQueueList() {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxHeight()
    ) {
        val lang by shared.i18n.LangState.collectAsState()

        Text(
            text = lang.emptyQueueList,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.subtitle2,
            modifier = Modifier.fillMaxWidth(),
            fontStyle = FontStyle.Italic
        )
    }
}