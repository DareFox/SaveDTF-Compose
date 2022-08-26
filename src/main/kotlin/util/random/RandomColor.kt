package util.random

fun randomColor(
    red: IntRange = 0..255,
    green: IntRange = 0..255,
    blue: IntRange = 0..255,
): RGB {
    return RGB(red.random(), green.random(), blue.random())
}

data class RGB(
    val red: Int,
    val green: Int,
    val blue: Int
) {
    fun toHex(): String {
        return red.toString(16) + green.toString(16) + blue.toString(16)
    }
}