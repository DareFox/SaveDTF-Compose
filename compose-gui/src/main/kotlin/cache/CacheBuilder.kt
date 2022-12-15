package cache

import shared.abstracts.StreamCache

fun buildCache(): StreamCache {
    return FileStreamCache
}
