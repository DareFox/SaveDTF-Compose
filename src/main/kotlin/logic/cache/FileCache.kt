package logic.cache

import mu.KotlinLogging
import util.filesystem.convertToValidName
import util.filesystem.getTempCacheFolder

/**
 * Represents cache on hard drive level.
 * Works not fast as [RamCache], but it can handle large amounts of data and can save it between sessions
 */
internal class FileCache(val subdirName: String? = null) : BinaryCache {
    private val logger = KotlinLogging.logger { }
    private val tempFolder
        get() = getTempCacheFolder(subdirName)

    override fun setValue(key: String, value: ByteArray) {
        tempFolder.resolve(convertToValidName(key)).writeBytes(value)
    }

    override fun getValueOrNull(key: String): ByteArray? {
        val validKey = convertToValidName(key)
        val file = tempFolder.resolve(validKey)
        return if (isExist(validKey)) {
            file.readBytes()
        } else {
            null
        }
    }

    override fun remove(key: String): Boolean {
        return tempFolder.resolve(convertToValidName(key)).deleteRecursively()
    }

    override fun isExist(key: String): Boolean {
        val file = tempFolder.resolve(convertToValidName(key))
        return file.exists() && file.isFile
    }

    override fun clearAll(): Boolean {
        logger.info { "Clearing all file cache" }
        return tempFolder.deleteRecursively()
    }
}