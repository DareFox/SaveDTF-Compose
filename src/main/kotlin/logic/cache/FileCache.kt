package logic.cache

import mu.KotlinLogging
import util.filesystem.convertToValidName
import util.filesystem.getTempCacheFolder
import util.filesystem.recursiveFileList
import util.filesystem.recursiveForEach
import java.io.File
import java.io.IOException

/**
 * Represents cache on hard drive level.
 * Works not fast as [RamCache], but it can handle large amounts of data and can save it between sessions
 */
internal class FileCache(val subdirName: String? = null) : BinaryCache {
    private val logger = KotlinLogging.logger { }
    private val tempFolder
        get() = getTempCacheFolder(subdirName)

    override fun setValue(key: String, value: ByteArray) {
        if (tempFolder.freeSpace < value.size) {
            logger.warn {
                "Can't save to file cache because disk ran out of space"
            }
        } else {
            tempFolder.resolve(convertToValidName(key)).writeBytes(value)

        }
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

    private fun makeRoomForFile(data: ByteArray) {
        val freeSpaceReservationInMb = 2048
        val freeSpaceReservationInBytes = freeSpaceReservationInMb * 1048576L
        val isEnoughSpaceForData = tempFolder.freeSpace - data.size >= freeSpaceReservationInBytes

        if (isEnoughSpaceForData) return

        val excessSize = (freeSpaceReservationInBytes - tempFolder.freeSpace) + data.size
        var releasedSpace = 0L

        for (file in tempFolder.recursiveFileList().sortedBy(File::lastModified)) {
            if (releasedSpace >= excessSize) {
                break;
            }

            try {
                val expectedReleasedSpaceBytes = file.length()

                if (file.delete()) {
                    releasedSpace += expectedReleasedSpaceBytes
                }
            } catch (ex: IOException) {
                logger.debug {
                    "Error during file deletion: [${ex.javaClass.simpleName}]  ${ex.message}\n" +
                            ex.stackTraceToString()
                }
            }
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
