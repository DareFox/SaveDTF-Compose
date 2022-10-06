package shared.document.operations

import kmtt.models.comment.Comment
import kmtt.models.entry.Entry
import kmtt.models.enums.SortingType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import shared.document.AbstractProcessorOperation
import mu.KotlinLogging
import org.jsoup.nodes.Document
import shared.i18n.Lang
import shared.util.dom.getWebsite
import shared.util.kmttapi.betterPublicKmtt
import java.io.File
import java.io.IOException

object SaveMetadata : AbstractProcessorOperation() {
    private val logger = KotlinLogging.logger { }
    private var cachedComments: List<Comment>? = null
    override val name: String
        get() = Lang.saveMetadataOperation
    private val cacheListeners = mutableListOf<(List<Comment>) -> Unit>()
    override suspend fun process(arguments: OperationArguments): Document {
        val entry = arguments.entry
        val document = arguments.document
        val folder = arguments.saveFolder

        if (entry == null) {
            return document
        }

        val website = document.getWebsite()
        val id = entry.id

        val comments = if (website != null && id != null) {
            cachedComments ?: try {
                val client = betterPublicKmtt(website)

                logger.info { "Trying to get comments metadata" }
                progress(Lang.saveMetadataOperationFetchComments)
                client.comments.getEntryComments(id, SortingType.POPULAR)
                    .also {
                        callListeners(it)
                    }
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
            progress(Lang.saveMetadataOperationWritingToFile)
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

    private fun callListeners(cache: List<Comment>) {
        cacheListeners.forEach {
            it(cache)
        }
    }

    fun setCachedComments(comments: List<Comment>) {
        cachedComments = comments
    }

    fun addCacheListener(listener: (List<Comment>) -> Unit) {
        cacheListeners += listener
    }
}

@Serializable
private data class EntryMetadata(val entry: Entry?, val comments: List<Comment>?)