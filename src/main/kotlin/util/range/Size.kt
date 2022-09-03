package util.range

val IntRange.size
    get() = lengthOfRange(this)

private fun lengthOfRange(
    range: IntRange
): Long {
    var result = 0L

    if (range.last == range.first) {
        return 1
    }

    if (range.first < 0) {
        when {
            range.last >= 0 -> {
                result += range.first * -1
                result += range.last
            }
            else -> { // range.last < // 0
                result += ((range.first * -1) + range.last).coerceAtLeast(0)
            }
        }
    } else { // range.first >= 0
        when {
            range.last >= 0 -> result += range.last - range.first
            else -> result += 0
        }
    }

    if (result != 0L) {
        result += 1
    }

    return result
}