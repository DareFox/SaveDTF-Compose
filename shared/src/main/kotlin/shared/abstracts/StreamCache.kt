package shared.abstracts

import java.io.InputStream
import java.io.OutputStream

interface StreamCache {
    /** Set value in cache **/
    fun setValue(key: String, value: InputStream)

    /** Set value in cache and redirect value to other [OutputStream]s **/
    fun setValue(key: String, value: InputStream, redirectTo: Collection<OutputStream>)

    /** Set value in cache and redirect value to other [OutputStream] **/
    fun setValue(key: String, value: InputStream, redirectTo: OutputStream)

    /** Get value from cache **/
    fun getValueOrNull(key: String): InputStream?

    /** Remove value from cache by key **/
    fun remove(key: String): Boolean

    /** Return `true` if value exists in cache, else `false` **/
    fun containsKey(key: String): Boolean

    /** Clear all cache **/
    fun clearAll(): Boolean
}