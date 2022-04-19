package util

import io.ktor.utils.io.errors.*
import java.io.File

fun convertToValidName(name: String): String {
    val file = File(name)
    val isValid = try {
        file.canonicalPath
        true
    } catch(_: IOException) {
        false
    }

    return if (isValid) {
        name
    } else {
        SharedRegex.filenameValidationRegex.replace(name, "")
    }
}