package cache

import shared.cache.StreamCache

fun buildCache(): StreamCache {
    return FileStreamCache
}
