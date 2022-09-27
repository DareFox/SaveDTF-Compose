package util.filesystem

import java.io.File

fun File.recursiveForEach(block: (File) -> Unit) {
    this.walk().forEach {
        // ignore folder where we're walking
        if (it == this) return@forEach

        if (it.isDirectory) {
            it.recursiveForEach(block)
        } else {
            block(it)
        }
    }
}

fun File.recursiveFileList(): List<File> {
    val list = mutableListOf<File>()

    this.walk().forEach {
        // ignore folder where we're walking
        if (it == this) return@forEach

        if (it.isDirectory) {
            list += it.recursiveFileList()
        } else {
            list += it
        }
    }

    return list
}