package shared.util.filesystem

import shared.util.kmttapi.KmttRegex
import java.io.File
import java.nio.file.InvalidPathException
import java.nio.file.Paths

fun convertToValidName(name: String, default: String = "null"): String {
    val newName = KmttRegex.filenameValidationRegex.replace(name, "")
    val file = File(newName)

    // Check if new name is valid, if not, return default
    return if (isValid(file)) {
        newName
    } else {
        default
    }
}

fun String.validatePath(default: String = "null"): String {
    return convertToValidName(this, default)
}

private fun isValid(file: File): Boolean {
    return try {
        Paths.get(file.path)
        true
    } catch (_: InvalidPathException) {
        false
    }

}