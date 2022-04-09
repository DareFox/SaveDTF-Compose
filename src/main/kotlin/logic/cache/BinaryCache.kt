package logic.cache

interface BinaryCache {
    fun setValue(key: String, value: ByteArray)
    fun getValueOrNull(key: String): ByteArray?

    fun remove(key: String): Boolean
    fun isExist(key: String): Boolean
}