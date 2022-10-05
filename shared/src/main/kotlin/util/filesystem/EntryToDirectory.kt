package util.filesystem

import kmtt.models.entry.Entry
import java.io.File
import java.util.*

fun Entry.toDirectory(baseFolder: File, subFolder: String? = null): File {
    val entryId = this.id ?: UUID.randomUUID().toString()
    val entryName = this.title ?: "no title"
    val entryFolder = convertToValidName("$entryId-$entryName", "$entryId-null")

    val authorId = this.author?.id ?: "unknown id"
    val authorName = this.author?.name ?: "unknown author"
    val authorFolder = convertToValidName("$authorId-$authorName", "$authorId-null")

    val subFolderText = if (subFolder == null) {
        ""
    } else {
        "$subFolder/"
    }

    val pathToSave = baseFolder.resolve("$subFolderText$authorFolder/$entryFolder")
    return pathToSave
}