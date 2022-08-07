package logic.cache

import mu.KotlinLogging
import util.cache.postfix
import util.cache.prefix
import util.filesystem.convertToValidName
import util.filesystem.getTempCacheFolder
import util.filesystem.recursiveFileList
import java.io.File
import java.io.IOException
import java.text.DecimalFormat

/**
 * Represents cache on hard drive level.
 * Works not fast as [RamCache], but it can handle large amounts of data and can save it between sessions
 */
internal class FileCache(val subdirName: String? = null) : BinaryCache {
    private val logger = KotlinLogging.logger { }
    private val tempFolder
        get() = getTempCacheFolder(subdirName)

    override fun setValue(key: String, value: ByteArray) {
        makeRoomForFile(value)
        if (tempFolder.freeSpace < value.size) {
            logger.warn {
                "Can't save to file cache because disk ran out of space"
            }
        } else {
            val file = tempFolder.resolve(convertToValidName(key))

            try {
                file.writeBytes(value)
            } catch (ex: Exception) {
                logger.error { "Caught error when trying to save cache to file:\n" +
                        "[${ex.javaClass.simpleName}]  ${ex.message}\n" +
                        ex.stackTraceToString()
                }
                try {
                    file.delete()
                } catch (_: Exception) {logger.error { "Can't clean file on error" }}
            }
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
        logger.info { "Not enough space for data. Trying to make room for file" }

        val excessSize = (freeSpaceReservationInBytes - tempFolder.freeSpace) + data.size
        var releasedSpace = 0L

        logger.info { "Excess size = (${sizeToString(excessSize)})" }
        logger.info { "File size = (${sizeToString(data.size.toLong())})" }
        logger.info { "Temp folder space = (${sizeToString(tempFolder.freeSpace)})" }
        for (file in tempFolder.recursiveFileList().sortedBy(File::lastModified)) {
            if (releasedSpace >= excessSize) {
                logger.info { "Released space (${sizeToString(releasedSpace)}) is bigger than excess size. Abort cleaning" }
                break;
            }

            try {
                val filename = file.name
                val isMetadataFile = filename.startsWith(prefix) && filename.endsWith(postfix)
                val fileList = mutableListOf(file)

                // Delete cached value with metadata too and vise versa
                if (isMetadataFile) {
                    val cachedFilename = filename.removePrefix(prefix).removeSuffix(postfix)
                    val cachedFile = file.resolveSibling(cachedFilename)

                    if (cachedFile.isFile) {
                        logger.debug { "Found cached pair value to delete" }
                        fileList += cachedFile
                    } else {
                        logger.debug { "Can't find cached pair value to delete" }
                    }
                } else {
                    val metadataFile = file.resolveSibling(prefix + filename + postfix)

                    if (metadataFile.isFile) {
                        logger.debug { "Found metadata pair value to delete" }
                        fileList += metadataFile
                    } else {
                        logger.debug { "Can't find metadata pair value to delete" }
                    }
                }

                fileList.forEach { deleteFile ->
                    val deletedFilename = deleteFile.name
                    val fileSizeInBytes = deleteFile.length()
                    if (deleteFile.delete()) {
                        logger.info {
                            "Deleted $deletedFilename file " +
                                    "(${sizeToString(fileSizeInBytes)})"
                        }
                        releasedSpace += fileSizeInBytes
                    }
                }
            } catch (ex: IOException) {
                logger.debug {
                    "Caught error during file deletion:\n" +
                            "[${ex.javaClass.simpleName}]  ${ex.message}\n" +
                            ex.stackTraceToString()
                }
            }
        }
    }

     private fun sizeToString(size: Long): String {
        val kilobyte = 1024
        val megabyte = kilobyte * 1024

        var result = 0f
        var type = ""

        when {
            size >= megabyte -> {
                result = size / megabyte.toFloat()
                type = "MB"
            }
            size >= kilobyte -> {
                result = size / kilobyte.toFloat()
                type = "KB"
            }
            else -> {
                return "$size B"
            }
        }

        val decimalFormat = DecimalFormat("#.##")
        return "${decimalFormat.format(result)} $type"
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