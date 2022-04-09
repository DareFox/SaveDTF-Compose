package util

import java.net.URL

private val dummy = object {}

fun readResource(path: String): URL {
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

    // Can read ONLY if path is '/foo/bar'
    // Neither './foo/bar' nor 'foo/bar'
    val res = dummy.javaClass.getResource(fixedPath)
    requireNotNull(res) {
        "Failed to read resource $fixedPath"
    }

    return res
}