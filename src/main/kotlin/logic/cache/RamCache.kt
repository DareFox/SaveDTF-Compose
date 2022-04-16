package logic.cache

import mu.KotlinLogging
import java.lang.ref.SoftReference
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.log

/**
 * Represents cache in RAM. Internally uses [FileCache] for saving cache between sessions and for saving up space in RAM
 */
internal class RamCache(val id: String? = null) : BinaryCache {
    private val logger = KotlinLogging.logger {  }
    private val cache: MutableMap<String, SoftReference<ByteArray>> = ConcurrentHashMap()
    private val fileCache = FileCache(id)

    override fun setValue(key: String, value: ByteArray) {
        logger.info { "Setting key $key with ByteArray size ${value.size}" }
        cache[key] = SoftReference(value)

        logger.info { "Calling FileCache to save value to disk" }
        fileCache.setValue(key, value)
    }

    /**
     * If value exists in disk, but not in RAM, this method will:
     *  - read value from disk
     *  - save it to RAM cache
     *  - return value
     */
    override fun getValueOrNull(key: String): ByteArray? {
        logger.info { "GetValueOrNull: key $key" }
        if (isExistInMemory(key)) {
            logger.info { "$key exists in memory" }
            val value = cache[key]!!.get()

            // This value can be collected by GC, so we check it on null
            if (value != null)
                logger.info { "$key wasn't collected by GC. Returning ByteArray with size of ${value.size}" }
                return value
        }

        // If no cached value in memory, load it from file.
        // If file doesn't exist, then return null
        return if (fileCache.isExist(key)) {
            logger.info { "$key doesn't exists in memory, but exists on hard drive" }
            val value = fileCache.getValueOrNull(key)
            if (value != null) {
                cache[key] = SoftReference(value)
            }

            value
        } else {
            logger.info { "No cached value with key $key. Returning null" }
            null
        }
    }

    override fun remove(key: String): Boolean {
        cache.remove(key)
        return fileCache.remove(key)
    }

    override fun isExist(key: String): Boolean {
        return isExistInMemory(key) || fileCache.isExist(key)
    }

    override fun clearAll(): Boolean {
        cache.clear()
        return fileCache.clearAll()
    }

    fun isExistInMemory(key: String): Boolean {
        // Check if GC collected value
        return cache.contains(key) && cache[key]!!.get() != null
    }
}