package util.string

import java.security.MessageDigest

fun String.sha256(): String = hashTo("SHA-256")
fun String.sha512(): String = hashTo("SHA-512")

private fun String.hashTo(algorithm: String): String {
    return MessageDigest.getInstance(algorithm).digest(
        toByteArray()
    ).fold("") { str, it -> str + "%02x".format(it) }
}