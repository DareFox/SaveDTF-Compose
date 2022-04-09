package logic.cache

import java.lang.ref.SoftReference
import java.util.concurrent.ConcurrentHashMap

class RamCache(val id: String? = null) : BinaryCache {
    private val cache: MutableMap<String, SoftReference<ByteArray>> = ConcurrentHashMap()
    private val fileCache = FileCache(id)

    override fun setValue(key: String, value: ByteArray) {
        cache[key] = SoftReference(value)
        fileCache.setValue(key, value)
    }

    override fun getValueOrNull(key: String): ByteArray? {
        if (isExistInMemory(key)) {
            val value = cache[key]!!.get()

            // This value can be collected by GC, so we check it on null
            if (value != null)
                return value
        }

        // If no cached value memory, load it from file.
        // If file doesn't exist, then return null
        return if (fileCache.isExist(key)) {
            val value = fileCache.getValueOrNull(key)
            if (value != null) {
                cache[key] = SoftReference(value)
            }

            value
        } else null
    }

    override fun remove(key: String): Boolean {
        cache.remove(key)
        return fileCache.remove(key)
    }

    override fun isExist(key: String): Boolean {
        return isExistInMemory(key) || fileCache.isExist(key)
    }

    fun isExistInMemory(key: String): Boolean {
        // Check if GC collected value
        return cache.contains(key) && cache[key]!!.get() != null
    }
}