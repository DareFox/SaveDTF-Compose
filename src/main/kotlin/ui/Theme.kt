package ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.em

fun String.color(): Color {
    return Color(this.removePrefix("#").toInt(16))
}

private val LightColorPalette = lightColors(
    primary = Color(0xFF6be9ff),
    primaryVariant = Color(0xFF14b7f2),
    onPrimary = Color(0xFF000000),

    secondary = Color(0xFFe88abf),
    secondaryVariant = Color(0xFFffbbf2),
    onSecondary = Color(0xFF000000),
    onError = Color(0xFFda1313),

)

private val DarkColorPalette = darkColors(
    primary = Color(0xFF81d4fa),
    primaryVariant = Color(0xFF4ba3c7),
    onPrimary = Color(0xFF000000),

    secondary = Color(0xFFe88abf),
    secondaryVariant = Color(0xFFffbbf2),
    onSecondary = Color(0xFF000000),
    onError = Color(0xFFEF4E4E),

)

val Lato = FontFamily(
    Font(resource = "font/Lato-Regular.ttf"),
    Font(resource = "font/Lato-Bold.ttf", FontWeight.Bold),
    Font(resource = "font/Lato-Light.ttf", FontWeight.Light)
)

val typography = Typography(
    subtitle1 = TextStyle(
        fontFamily = Lato,
        fontSize = 1.6.em,
        fontWeight = FontWeight.ExtraLight,
    ),
    subtitle2 = TextStyle(
        fontFamily = Lato,
        fontSize = 1.3.em,
        fontWeight = FontWeight.ExtraLight,
    ),
    h1 = TextStyle(
        fontSize = 3.0.em,
        fontFamily = Lato,
        fontWeight = FontWeight.Bold,
    ),
    h2 = TextStyle(
        fontSize = 2.5.em,
        fontFamily = Lato,
        fontWeight = FontWeight.Bold,
    ),
    h3 = TextStyle(
        fontSize = 2.0.em,
        fontFamily = Lato,
        fontWeight = FontWeight.Bold
    ),
    h4 = TextStyle(
        fontSize = 1.7.em,
        fontFamily = Lato,
        fontWeight = FontWeight.Bold
    ),

)

@Composable
fun SaveDtfTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        content = content,
        typography = typography
    )
}