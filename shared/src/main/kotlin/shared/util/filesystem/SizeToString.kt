package shared.util.filesystem

import java.text.DecimalFormat

fun sizeToString(bytesAmount: Long): String {
    val kilobyte = 1024
    val megabyte = kilobyte * 1024

    var result = 0f
    var type = ""

    when {
        bytesAmount >= megabyte -> {
            result = bytesAmount / megabyte.toFloat()
            type = "MB"
        }
        bytesAmount >= kilobyte -> {
            result = bytesAmount / kilobyte.toFloat()
            type = "KB"
        }
        else -> {
            return "$bytesAmount B"
        }
    }

    val decimalFormat = DecimalFormat("#.##")
    return "${decimalFormat.format(result)} $type"
}