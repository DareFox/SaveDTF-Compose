fun String.repeatOrEmpty(num: Int = 1): String {
    if (num <= 0) {
        return ""
    } else {
        var result = this
        for (number in 0..num) {
            result += this
        }
        return result
    }
}