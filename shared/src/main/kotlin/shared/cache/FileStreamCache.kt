package shared.cache

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import shared.io.WaitingMutex
import shared.io.asCounter
import shared.io.asMultiStream
import mu.KotlinLogging
import shared.util.filesystem.getTempCacheFolder
import shared.util.filesystem.recursiveFileList
import shared.util.filesystem.sizeToString
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*


object FileStreamCache : StreamCache {
    private val tempFolder = getTempCacheFolder()
    private val fileMap = FileMap.createMap(tempFolder)
    private val logger = KotlinLogging.logger { }
    private val waitingMutex = WaitingMutex(CoroutineScope(Dispatchers.IO + SupervisorJob()))
    private val freeSpaceReservationInMb: Long = 2048
    private val maxSizeOfFileCacheInMb: Long = 4096

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
            "Saved key $key to file ${file.absolutePath}. Size of file: ${sizeToString(counterFileStream.counter)}"
        }
        fileMap.addKey(key, file)
        makeRoomForFile()
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

    private fun makeRoomForFile() {
        waitingMutex.tryToRun {
            val freeSpaceReservationInBytes = freeSpaceReservationInMb * 1048576L
            val maxSizeOfFileCacheInBytes = maxSizeOfFileCacheInMb * 1048576L

            val files = tempFolder.recursiveFileList()

            // File.length() on directory returns unspecified value
            // So we need to sum files length
            val folderLength: Long = files.sumOf { it.length() }

            val isNotEnoughSpace = tempFolder.freeSpace < freeSpaceReservationInBytes
            val fileCacheMaxedOut = folderLength >= maxSizeOfFileCacheInBytes

            val excessReservedSpace = freeSpaceReservationInBytes - tempFolder.freeSpace
            val excessCachedMaxedOutSpace = folderLength - maxSizeOfFileCacheInBytes

            if (isNotEnoughSpace) {
                logger.info { "Not enough space for data. Trying to make room for file" }
            }

            if (fileCacheMaxedOut) {
                logger.info { "File cached maxed out. Trying to make room for file" }
            }

            if (!isNotEnoughSpace && !fileCacheMaxedOut) {
                delay(1500L)
                return@tryToRun
            }

            // Math.max(), but Kotlin way
            val toRelease = excessReservedSpace.coerceAtLeast(excessCachedMaxedOutSpace)

            logger.info { "Excess size = (${sizeToString(toRelease)})" }
            logger.info { "Temp folder free space = (${sizeToString(tempFolder.freeSpace)})" }
            logger.info { "Temp folder size = (${sizeToString(folderLength)})" }

            freeBytes(toRelease)
            delay(1500L)
        }
    }

    private fun freeBytes(bytes: Long) {
        val nonExistentFiles = mutableMapOf<File, MutableList<String>>()
        var releasedSpace = 0L

        val canonicalFileToKeys = fileMap.map.mapNotNull {
            try {
                it.value.canonicalFile to it.key
            } catch (e: Exception) {
                nonExistentFiles[it.value] = nonExistentFiles.getOrDefault(it.value, mutableListOf()).apply {
                    add(it.key)
                }
                null
            }
        }.groupBy({ it.first }) {
            it.second
        }

        nonExistentFiles.entries.forEach {
            val size = it.key.length()
            if (tryToDelete(it.key)) {
                logger.info {
                    "Deleted ${it.key.absolutePath} file (${sizeToString(size)})"
                }
                releasedSpace += size
            }
            it.value.forEach { key ->
                fileMap.removeKey(key)
            }

            if (releasedSpace >= bytes) {
                logger.info {
                    "Released space (${sizeToString(releasedSpace)}) is bigger than given (${sizeToString(bytes)}) size. Abort cleaning"
                }
                return
            }
        }

        val files = tempFolder.recursiveFileList()

        /**
         * Why randomize?
         *
         * Because deleting files without any regard to how often or
         * how many times they were accessed before is inefficient.
         *
         * ...and I'm too lazy to create history access based cache
         */
        for (file in files.shuffled()) {
            // Don't delete cache manifest file
            if (file.extension == "json") continue

            val deletedFilename = file.name
            val canonical = try {
                file.canonicalFile
            } catch (e: Exception) {
                null
            }
            val fileSizeInBytes = file.length()

            if (tryToDelete(file)) {
                logger.info {
                    "Deleted $deletedFilename file " +
                            "(${sizeToString(fileSizeInBytes)})"
                }
                releasedSpace += fileSizeInBytes

                if (canonical != null) {
                    canonicalFileToKeys[canonical]?.forEach {
                        fileMap.removeKey(it)
                    }
                }
            }

            if (releasedSpace >= bytes) {
                logger.info {
                    "Released space (${sizeToString(releasedSpace)}) is bigger than given (${sizeToString(bytes)}) size. Abort cleaning"
                }
                return
            }
        }
    }

    private fun tryToDelete(file: File): Boolean {
        return try {
            file.delete()
        } catch (ex: IOException) {
            logger.warn {
                "Caught error during file deletion:\n" +
                        "[${ex.javaClass.simpleName}]  ${ex.message}\n" +
                        ex.stackTraceToString()
            }
            false
        }
    }
}


