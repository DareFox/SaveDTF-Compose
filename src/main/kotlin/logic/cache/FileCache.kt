package logic.cache

import util.getTempCacheFolder

class FileCache(val subdirName: String? = null): BinaryCache {
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
}