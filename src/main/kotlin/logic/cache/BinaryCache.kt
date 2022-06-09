package logic.cache

interface BinaryCache {
    /** Set value in cache **/
    fun setValue(key: String, value: ByteArray)

    /** Get value from cache **/
    fun getValueOrNull(key: String): ByteArray?

    /** Remove value from cache by key **/
    fun remove(key: String): Boolean

    /** Return `true` if value exists in cache, else `false` **/
    fun isExist(key: String): Boolean

    /** Clear all cache **/
    fun clearAll(): Boolean
}