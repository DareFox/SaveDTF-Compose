package logic.document.operations

import kmtt.models.comment.Comment
import kmtt.models.entry.Entry
import kmtt.models.enums.SortingType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import logic.document.AbstractProcessorOperation
import mu.KotlinLogging
import org.jsoup.nodes.Document
import util.dom.getWebsite
import util.kmttapi.betterPublicKmtt
import java.io.File
import java.io.IOException

class SaveMetadata(val entry: Entry, val folder: File): AbstractProcessorOperation() {
    private val logger = KotlinLogging.logger {  }

    override val name: String = "Save metadata"

    override suspend fun process(document: Document): Document {
        val website = document.getWebsite()
        val id = entry.id

        val comments = if (website != null && id != null) {
            try {
                val client = betterPublicKmtt(website)
                logger.info { "Trying to get comments metadata" }
                client.comments.getEntryComments(id, SortingType.POPULAR)
            } catch (e: Exception) {
                logger.error(e) {
                    "Can't get comments from entry $id"
                }

                null
            }
        } else {
            logger.info { "List of comments is null because one of the value is null: (website=$website, id=$id)" }
            null
        }

        val meta = EntryMetadata(entry, comments)

        try {
            logger.info { "Trying to save metadata to .json file in ${folder.absolutePath}" }

            val jsonFile = folder.resolve("metadata.json")

            withContext(Dispatchers.IO) {
                folder.mkdirs()
                jsonFile.createNewFile()
            }

            Json.encodeToStream(meta, jsonFile.outputStream())

            logger.info { "Saved metadata!" }
        } catch (e: IOException) {
            logger.error(e) {
                "Caught error during writing metadata file"
            }
        }

        return document
    }
}

@Serializable
private data class EntryMetadata(val entry: Entry?, val comments: List<Comment>?)