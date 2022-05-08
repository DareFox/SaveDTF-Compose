package util

import io.ktor.utils.io.errors.*
import java.io.File
import java.nio.file.InvalidPathException
import java.nio.file.Paths

fun convertToValidName(name: String, default: String = "null"): String {
    val file = File(name)
    val newName = if (isValid(file)) {
        name
    } else {
        SharedRegex.filenameValidationRegex.replace(name, "")
    }

    // Check if new name is valid, if not, return default
    return if(isValid(File(newName))) {
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