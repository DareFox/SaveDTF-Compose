package util.cache

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import logic.cache.BinaryCache
import logic.cache.RamCache

const val prefix = "METADATA-"
const val postfix = ".json"

/**
 * Set value and metadata in cache
 *
 * @sample setValueSample
 * @see getValueWithMetadata
 */
internal inline fun <reified T> BinaryCache.setValueWithMetadata(key: String, binary: ByteArray, metadata: T) {
    this.setValue(key, binary)
    this.setValue(prefix + key + postfix, Json.encodeToString(metadata).toByteArray())
}

/**
 * Get value with saved metadata
 *
 * @sample getValueSample
 * @see setValueWithMetadata
 */
internal inline fun <reified T> BinaryCache.getValueWithMetadata(key: String): Pair<ByteArray, T?>? {
    val value = getValueOrNull(key)
    val metadata = getValueOrNull(prefix + key + postfix)

    return if (value != null) {
        val parsedMetadata: T? = if (metadata != null) {
            Json.decodeFromString<T>(String(metadata))
        } else {
            null
        }

        Pair(value, parsedMetadata)
    } else {
        null
    }
}


private fun setValueSample() {
    val cache = RamCache() // or any cache that implements BinaryCache

    @kotlinx.serialization.Serializable // This annotation is important!
    // Because without this annotation, you can't encode/decode metadata object
    data class Metadata(val name: String)

    cache.setValueWithMetadata("testing", "data".toByteArray(), Metadata("No Name")) // Cache value
    // Under the hood
    // Set key "testing" to Binary array of text "data"
    // Set key "METADATA-testing.json" to Metadata object
}

private fun getValueSample() {
    val cache = RamCache() // or any cache that implements BinaryCache

    @kotlinx.serialization.Serializable // This annotation is important!
    // Because without this annotation, you can't encode/decode metadata object
    data class Metadata(val name: String)

    // Get value from cache. Returns nullable type
    val value = cache.getValueWithMetadata<Metadata>("testing")

    // Under hood:
    // Read value from "testing" key
    // Read value from "METADATA-testing.json"
    // Combine it in Pair and return it


    value?.first // Binary data in ByteArray
    value?.second // Nullable metadata object
}