package logic.cache

import java.util.concurrent.ConcurrentHashMap

enum class CacheType {
    RAM,
    HARD_DRIVE
}

private val cachedCaches: MutableMap<Pair<String?, CacheType>, BinaryCache> = ConcurrentHashMap() // nice naming

fun buildCache(
    id: String? = null,
    cacheType: CacheType = CacheType.RAM,
    preventCacheDuplication: Boolean = true,
): BinaryCache {
    val cache: BinaryCache

    if (preventCacheDuplication) {
        // Use double-check lock to prevent duplicate (https://en.wikipedia.org/wiki/Double-checked_locking#Usage_in_Java)
        cache = cachedCaches[id to cacheType] ?: synchronized(cachedCaches) {
            cachedCaches[id to cacheType] ?: createCache(cacheType, id).also {
                cachedCaches[id to cacheType] = it
            }
        }
    } else {
        cache = createCache(cacheType, id)
    }

    return cache
}

private fun createCache(type: CacheType, id: String?): BinaryCache {
    return when (type) {
        CacheType.RAM -> RamCache(id)
        CacheType.HARD_DRIVE -> FileCache(id)
    }
}