package util.random

import util.range.size
import kotlin.random.Random
import kotlin.random.nextInt

fun randomColor(
    red: IntRange = 0..255,
    green: IntRange = 0..255,
    blue: IntRange = 0..255,
): RGB {
    return RGB(red.random(), green.random(), blue.random())
}

fun offsetRandomColor (
    color: RGB,
    offset: Int,
    colorAmountOffset: Int = 2,
    colorRange: IntRange = 0..255,
): RGB {
    require(colorAmountOffset >= 1) {
        "Amount of colors to offset can't be less than 1"
    }

    require(offset >= 0) {
        "Value offset can't be negative"
    }

    val red: IntRange
    val green: IntRange
    val blue: IntRange

    if (colorAmountOffset >= 3) {
        red = offsetRange(offset, color.red, colorRange)
        green = offsetRange(offset, color.green, colorRange)
        blue = offsetRange(offset, color.blue, colorRange)
    } else {
        val shuffleRed: Boolean
        val shuffleGreen: Boolean
        val shuffleBlue : Boolean

        if (colorAmountOffset == 2) { // 2 colors needs to be shuffled
            shuffleRed = Random.nextBoolean()

            // If shuffleRed is false, then 2 remaining colors always needs to be shuffled
            // If shuffleGreen is true, randomize boolean
            shuffleGreen = if (!shuffleRed) true else Random.nextBoolean()

            // If red and green are shuffled, then we return false
            // Else we indicate blue to shuffle
            shuffleBlue = !(shuffleRed && shuffleGreen)
        } else { // 1 color needs to be shuffled

            shuffleRed = Random.nextBoolean()

            // If red shuffled then ignore, else randomize boolean
            shuffleGreen = if (shuffleRed) false else Random.nextBoolean()

            // If red AND green are both not shuffled, then we shuffle blue because it's last color
            shuffleBlue = !shuffleRed && !shuffleGreen
        }

        red = if (shuffleRed) offsetRange(offset, color.red, colorRange) else colorRange
        green = if (shuffleGreen) offsetRange(offset, color.green, colorRange) else colorRange
        blue = if (shuffleBlue) offsetRange(offset, color.blue, colorRange) else colorRange
    }

    return RGB(red.random(), green.random(), blue.random())
}

/**
 * Splits range on two parts in [from] point with given [offset] and returns random part
 *
 * Chance of return depends on size of new range
 *
 * Check image explanation (offsetRange.png in this folder)
 */
private fun offsetRange(offset: Int, from: Int, range: IntRange): IntRange {
    val start = range.first..(from - offset).coerceAtLeast(minimumValue = range.first)
    val end = (from + offset).coerceAtMost(maximumValue = range.last)..range.last

    val total = start.size + end.size
    val random = Random.nextLong(0L, total) + 1
    val useStart = random <= start.size


    return if (useStart) start else end
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