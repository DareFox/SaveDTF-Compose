
package logic.cache

import logic.io.asCounter
import logic.io.asMultiStream
import mu.KotlinLogging
import util.filesystem.getTempCacheFolder
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.*


object FileStreamCache: StreamCache {
    private val tempFolder = getTempCacheFolder()
    private val fileMap = FileMap.createMap(tempFolder)
    private val logger = KotlinLogging.logger {  }

    override fun setValue(key: String, value: InputStream) {
        setValue(key, value, emptyList())
    }

    override fun setValue(key: String, value: InputStream, redirectTo: Collection<OutputStream>) {
        var file: File? = null

        while (file?.exists() != false) {
            file = tempFolder.resolve(UUID.randomUUID().toString())
        }

        file.parentFile.mkdirs()
        file.createNewFile()

        val counterFileStream = file.outputStream().asCounter()

        value.copyTo((redirectTo + counterFileStream).asMultiStream())
        logger.debug {
            "Saved key $key to file ${file.absolutePath}. Size of file: $"
        }
        fileMap.addKey(key, file)
    }

    override fun setValue(key: String, value: InputStream, redirectTo: OutputStream) {
        setValue(key, value, listOf(redirectTo))
    }

    override fun getValueOrNull(key: String): InputStream? {
        return fileMap.getKey(key)?.inputStream()
    }

    override fun remove(key: String): Boolean {
        return fileMap.getKey(key)?.delete().also {
            fileMap.removeKey(key)
        } ?: true
    }

    override fun containsKey(key: String): Boolean {
        return fileMap.containsKey(key)
    }

    override fun clearAll(): Boolean {
        return tempFolder.deleteRecursively().also {
            if (it) fileMap.clearAll()
        }
    }
}


