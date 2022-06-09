package util.kmttapi

/**
 * Get media id from Leonardo (media service). Returns `null` if url isn't valid
 *
 * @sample getIdOrNullSample
 */
fun String.getMediaIdOrNull(): String? {
    val regex = Regex("""(?<=leonardo\.osnova\.io\/)\S+?[^\/](?=\/|${'$'}|\s+)""")
    val search = regex.find(this)
    return search?.value
}

/**
 * Get media id from Leonardo (media service). Throws [IllegalArgumentException] if url isn't valid
 *
 * @sample getIdSample
 */
fun String.getMediaId(): String {
    val value = this.getMediaIdOrNull()

    requireNotNull(value) {
        "$this is not a valid leonardo.osnova.io (media) url "
    }

    return value
}

private fun getIdOrNullSample() {
    val validURL = "https://leonardo.osnova.io/118ce574-1507-5a19-92a0-6b06da18fdf3/"
    validURL.getMediaIdOrNull() // 118ce574-1507-5a19-92a0-6b06da18fdf3

    val noSlashURL = "https://leonardo.osnova.io/118ce574-1507-5a19-92a0-6b06da18fdf3"
    noSlashURL.getMediaIdOrNull() // 118ce574-1507-5a19-92a0-6b06da18fdf3

    val invalidURL = "https://osnova.io/118ce574-1507-5a19-92a0-6b06da18fdf3"
    invalidURL.getMediaIdOrNull() // null
}

private fun getIdSample() {
    val validURL = "https://leonardo.osnova.io/118ce574-1507-5a19-92a0-6b06da18fdf3/"
    validURL.getMediaId() // 118ce574-1507-5a19-92a0-6b06da18fdf3

    val noSlashURL = "https://leonardo.osnova.io/118ce574-1507-5a19-92a0-6b06da18fdf3"
    noSlashURL.getMediaId() // 118ce574-1507-5a19-92a0-6b06da18fdf3

    val invalidURL = "https://osnova.io/118ce574-1507-5a19-92a0-6b06da18fdf3"
    invalidURL.getMediaId() // IllegalArgumentException
}
