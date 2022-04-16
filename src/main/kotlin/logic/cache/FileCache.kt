package logic.cache

import util.getTempCacheFolder

/**
 * Represents cache on hard drive level.
 * Works not fast as [RamCache], but it can handle large amounts of data and can save it between sessions
 */
internal class FileCache(val subdirName: String? = null): BinaryCache {
    private val tempFolder
        get() = getTempCacheFolder(subdirName)

    override fun setValue(key: String, value: ByteArray) {
        tempFolder.resolve(key).writeBytes(value)
    }

    override fun getValueOrNull(key: String): ByteArray? {
        val file = tempFolder.resolve(key)
        return if (isExist(key)) {
            file.readBytes()
        } else {
            null
        }
    }

    override fun remove(key: String): Boolean {
        return tempFolder.resolve(key).deleteRecursively()
    }

    override fun isExist(key: String): Boolean {
        val file = tempFolder.resolve(key)
        return file.exists() && file.isFile
    }

    override fun clearAll(): Boolean {
        return tempFolder.deleteRecursively()
    }
}