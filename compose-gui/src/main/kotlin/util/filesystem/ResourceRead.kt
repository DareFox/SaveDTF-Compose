package util.filesystem

import java.net.URL

private val dummy = object {}

/**
 * Read resource at path
 */
private fun readResourceOrNull(path: String, fixedPathCallback: (String) -> Unit): URL? {
    // If string starts with ./ or .\
    // e.g ./foo/bar or .\foo\bar
    val dotSlashRegex = """^.(/ | \\)""".toRegex()

    // If string doesn't start with /
    // e.g. foo/bar
    val noSlashStartRegex = """^[^/]""".toRegex()

    var fixedPath = path

    // Remove ./ at start
    fixedPath = fixedPath.replace(dotSlashRegex, "")

    // Add / slash at start
    fixedPath = fixedPath.replace(noSlashStartRegex) {
        "/" + it.value
    }

    // Notify fixed path
    fixedPathCallback(fixedPath)

    // Can read ONLY if path is '/foo/bar'
    // Neither './foo/bar' nor 'foo/bar'
    return dummy.javaClass.getResource(fixedPath)
}

/**
 * Read resource at path
 *
 * Throws [IllegalArgumentException] if failed to load resource
 */
fun readResource(path: String): URL {
    var fixedPath = path

    val res = readResourceOrNull(path) {
        fixedPath = it
    }

    requireNotNull(res) {
        "Failed to read resource $path. [Fixed path is $fixedPath]"
    }

    return res
}