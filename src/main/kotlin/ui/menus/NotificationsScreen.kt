package ui.menus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ui.composables.notifications.NotificationList

@Composable
fun NotificationsUI() {
    Box(modifier = Modifier.fillMaxSize().padding(top = 10.dp), contentAlignment = Alignment.TopCenter) {
        NotificationList()
    }
}