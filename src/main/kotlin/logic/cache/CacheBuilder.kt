package logic.cache

import java.util.concurrent.ConcurrentHashMap

enum class CacheType {
    RAM,
    HARD_DRIVE
}

private val cachedCaches: MutableMap<Pair<String?, CacheType>, BinaryCache> = ConcurrentHashMap() // nice naming

@Synchronized
fun buildCache(
    id: String? = null,
    cacheType: CacheType = CacheType.RAM,
    preventCacheDuplication: Boolean = true,
): BinaryCache {
    val cache: BinaryCache

    if (preventCacheDuplication && cachedCaches.containsKey(id to cacheType)) {
        cache = cachedCaches[id to cacheType]!!
    } else {
        cache = when (cacheType) {
            CacheType.RAM -> RamCache(id)
            CacheType.HARD_DRIVE -> FileCache(id)
        }

        if (preventCacheDuplication) {
            cachedCaches[id to cacheType] = cache
        }
    }

    return cache
}