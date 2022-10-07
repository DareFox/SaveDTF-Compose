package ui.composables.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.X
import shared.i18n.Lang
import ui.theme.CustomPallet
import viewmodel.NotificationData
import viewmodel.NotificationType

@Composable
fun SimpleNotification(notificationData: NotificationData, onClose: () -> Unit) {
    val mainColor = when (notificationData.type) {
        NotificationType.INFO -> MaterialTheme.colors.primary
        NotificationType.ERROR -> MaterialTheme.colors.error
        NotificationType.SUCCESS -> CustomPallet.success
    }

    val onColor = when (notificationData.type) {
        NotificationType.INFO -> MaterialTheme.colors.onPrimary
        NotificationType.ERROR -> CustomPallet.onError
        NotificationType.SUCCESS -> CustomPallet.onSuccess
    }

    Notification(mainColor, onColor, onClose) {
        Text(notificationData.text)
    }
}

@Composable
fun Notification(
    borderColor: Color,
    iconColor: Color,
    onClose: () -> Unit,
    composable: @Composable () -> Unit,
) {
    val lang by shared.i18n.LangState.collectAsState()
    val shape = RoundedCornerShape(30)

    Surface(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(65.dp)
            .clip(shape)
            .border(4.dp, borderColor, shape),
        color = MaterialTheme.colors.background
    ) {
        Box(contentAlignment = Alignment.Center) {
            composable()
        }
    }

    Box(contentAlignment = Alignment.TopEnd, modifier = Modifier.fillMaxWidth(0.8f)) {
        Box(
            Modifier
                .background(borderColor, shape)
                .border(4.dp, borderColor, shape)
        ) {
            Icon(
                imageVector = FeatherIcons.X,
                contentDescription = lang.notificationCloseDescription,
                modifier = Modifier.clickable {
                    onClose()
                },
                tint = iconColor
            )
        }
    }

}