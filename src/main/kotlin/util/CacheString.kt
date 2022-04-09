package util

import logic.cache.BinaryCache
import java.nio.charset.Charset

fun BinaryCache.setValue(key: String, value: String, charset: Charset = Charsets.UTF_8) {
    return setValue(key, value.toByteArray(charset))
}