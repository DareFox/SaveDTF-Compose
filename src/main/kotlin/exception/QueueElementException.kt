package exception

open class QueueElementException(errorMessage: String) : Throwable(errorMessage)

fun <T : Any> T?.errorOnNull(errorMessage: String): T {
    return this ?: throw QueueElementException(errorMessage)
}