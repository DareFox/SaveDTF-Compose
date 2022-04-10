package util

import java.io.File

private const val folderName: String = "saveDTF-cache"

/**
 * Get temp folder. If folder doesn't exists — create it
 *
 * @sample sample
 */
fun getTempCacheFolder(resolve: String? = null): File {
    val temp = File(System.getProperty("java.io.tmpdir")).resolve(folderName)
    temp.mkdirs()

    return if (resolve != null) {
        val resolvedPath = temp.resolve(resolve)
        resolvedPath.mkdirs()

        resolvedPath
    } else {
        temp
    }
}

private fun sample() {
    getTempCacheFolder() // File(%temp%/saveDTF-cache)
    getTempCacheFolder("someCoolName") // File(%temp%/saveDTF-cache/someCoolName)
}

