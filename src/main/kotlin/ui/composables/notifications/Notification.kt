package ui.composables.notifications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.X
import compose.icons.feathericons.XCircle
import javax.management.Notification

@Composable
fun Notification(text: String, onClose: () -> Unit) {
    val shape = RoundedCornerShape(30)

    Surface(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(65.dp)
            .clip(shape)
            .border(4.dp, MaterialTheme.colors.primary, shape),
        color = MaterialTheme.colors.background
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text)
        }
    }

    Box(contentAlignment = Alignment.TopEnd, modifier = Modifier.fillMaxWidth(0.8f)) {
        Box(Modifier
            .background(MaterialTheme.colors.primary, shape)
            .border(4.dp, MaterialTheme.colors.primary, shape)) {
            Icon(
                imageVector = FeatherIcons.X,
                contentDescription = "Close notification",
                modifier = Modifier.clickable {
                    onClose()
                },
                tint = MaterialTheme.colors.onPrimary
            )
        }
    }

}