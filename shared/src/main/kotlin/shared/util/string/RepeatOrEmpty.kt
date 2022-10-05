package shared.util.string

fun String.repeatOrEmpty(num: Int = 1): String {
    return if (num <= 0) {
        ""
    } else {
        var result = this
        for (number in 0..num) {
            result += this
        }
        result
    }
}