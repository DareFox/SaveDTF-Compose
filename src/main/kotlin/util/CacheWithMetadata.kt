package util

import kmtt.util.jsonParser
import logic.cache.BinaryCache
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

const val prefix = "METADATA-"
const val postfix = ".json"

internal inline fun <reified T> BinaryCache.cacheWithMetadata(key: String, binary: ByteArray, metadata: T) {
    this.setValue(key, binary)
    this.setValue(prefix + key + postfix, jsonParser.encodeToString(metadata).toByteArray())
}



internal inline fun <reified T> BinaryCache.getValueWithMetadata(key: String): Pair<ByteArray, T?>? {
    val value = getValueOrNull(key)
    val metadata = getValueOrNull(prefix + key + postfix)

    return if (value != null) {
        val parsedMetadata: T? = if (metadata != null) {
            jsonParser.decodeFromString<T>(String(metadata))
        } else {
            null
        }

        Pair(value, parsedMetadata)
    } else {
        null
    }
}
