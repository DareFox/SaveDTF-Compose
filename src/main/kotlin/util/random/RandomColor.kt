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
        return red.toString(16).padStart(2, '0') +
                green.toString(16).padStart(2, '0') +
                blue.toString(16).padStart(2, '0')
    }
}