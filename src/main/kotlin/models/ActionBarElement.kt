package models

import androidx.compose.ui.graphics.vector.ImageVector

data class ActionBarElement<T>(val icon: ImageVector, val description: String, val onClickCallback: (T) -> Unit)