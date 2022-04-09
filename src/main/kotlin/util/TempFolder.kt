package util

import java.io.File

private const val folderName: String = "saveDTF-cache"

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