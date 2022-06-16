package util.filesystem

import kmtt.models.entry.Entry
import java.io.File
import java.util.*

fun Entry.toDirectory(baseFolder: File): File {
    val entryId = this.id ?: UUID.randomUUID().toString()
    val entryName = this.title ?: "no title"
    val entryFolder = convertToValidName("$entryId-$entryName", "$entryId-null")

    val authorId = this.author?.id ?: "unknown id"
    val authorName = this.author?.name ?: "unknown author"
    val authorFolder = convertToValidName("$authorId-$authorName", "$authorId-null")

    val pathToSave = baseFolder.resolve("entry/$authorFolder/$entryFolder")
    return pathToSave
}