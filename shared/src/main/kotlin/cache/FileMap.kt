package logic.cache

import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import logic.io.WaitingMutex
import mu.KotlinLogging
import java.io.File

/**
 * Map that allows to associate **any** string key to the file
 *
 * Very useful when you need to associate key to a File, but naming file as key
 * is impossible due to illegal characters in filename (e.g. URL)
 *
 * Also, it saves changes between sessions in file and load it on [creation][FileMap.createMap]
 */
class FileMap private constructor(
    private val directory: File,
    private val _map: MutableMap<String, File>,
    private val metadataFile: File
) {
    val map: Map<String, File>
        get() = _map

    private val logger = KotlinLogging.logger { }
    private val waitMutex = WaitingMutex(CoroutineScope(Dispatchers.IO + SupervisorJob()))

    /**
     * Create key to file association
     *
     * Given [relativePath] string will be [resolved as relative][File.resolve] of given during build [directory]
     *
     * If file doesn't exist, then manager will not add key
     */
    fun addKey(key: String, relativePath: String) {
        addKey(key, directory.resolve(relativePath))
    }

    /**
     * Create key to file association
     *
     * If file doesn't exist, then manager will not add key
     */
    fun addKey(key: String, file: File) {
        if (file.exists()) {
            val previous = _map[key]
            _map[key] = file

            // this function runs on another thread and will not block this
            // so don't delay save by deleting previous file
            trySave()

            // File can be used two times or more
            // TODO: Add check if another key uses file
            if (previous?.exists() == true) {
                previous.delete()
            }
        }
    }

    /**
     * Add key-file pair to manager
     *
     * If file doesn't exist, then manager will not add key
     */
    fun addKey(keyFilePair: Pair<String, File>) {
        addKey(keyFilePair.first, keyFilePair.second)
    }

    /**
     * Get file associated with the given key
     *
     * If file is no more exists, then key will be removed
     * and function will return null
     */
    fun getKey(key: String): File? {
        val file = _map[key] ?: return null

        return if (file.exists() && file.isFile) {
            logger.debug { "Returning ${file.absolutePath} for $key" }
            file
        } else {
            _map.remove(key)
            trySave()
            null
        }
    }

    /**
     * Try to save current map to a file for using it between sessions
     *
     * If program already in process of saving, this function will wait
     * (**in another thread**) end of the saving job and start it over again
     *
     * *P.S. Multiple calls will not create multiple waiting threads.
     * If one thread is already waiting, then program will not create another one*
     */
    // All this function calls could be done with observable map, but im lazy to implement it
    // And I don't want to use another 3rd party lib for this
    private fun trySave() {
        waitMutex.tryToRun {
            saveMetadata()
        }
    }

    /**
     * Save map to JSON file with buffer delay of 500ms
     */
    private suspend fun saveMetadata() {
        logger.debug { "saveMetadata - Trying to save map to ${metadataFile.absolutePath}" }
        val json = json.encodeToString(_map.mapValues {
            it.value.absolutePath
        })
        withContext(Dispatchers.IO) {
            metadataFile.parentFile.mkdirs()
            metadataFile.createNewFile()
        }
        metadataFile.writeText(json)
        logger.debug { "saveMetadata - Finished saving, delaying next call" }

        // Buffer changes
        delay(500L)
    }

    /**
     * Remove key-file pair from map
     */
    fun removeKey(key: String) {
        _map.remove(key)?.let {
            trySave()
        }
    }

    /**
     * Remove **all** key-file pairs from map
     */
    fun clearAll() {
        _map.clear()
        trySave()
    }

    /**
     * Checks if file associated with given key exists
     *
     * If file is no longer present, it will return false and remove key from map
     */
    fun containsKey(key: String): Boolean {
        return _map[key]?.exists()?.also {
            if (!it) removeKey(key)
        } ?: false
    }

    companion object {
        private val json = Json

        /**
         * Create or load saved [FileMap] from serialized file in specified [directory]
         *
         * If file doesn't exist, it will create an empty [FileMap]
         */
        fun createMap(directory: File): FileMap {
            directory.mkdirs()

            val metadataFilename = "filemanager-data.json"
            val file = directory.resolve(metadataFilename).apply { createNewFile() }

            val map = if (file.exists() && file.isFile) {
                try {
                    json.decodeFromStream<MutableMap<String, String>>(file.inputStream()).mapValues {
                        File(it.value)
                    }.toMutableMap()
                } catch (ex: Exception) {
                    mutableMapOf()
                }
            } else {
                mutableMapOf()
            }

            return FileMap(directory, map, file)
        }
    }
}