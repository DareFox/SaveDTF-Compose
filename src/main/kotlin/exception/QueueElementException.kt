package exception

open class QueueElementException(val errorMessage: String) : Throwable()

fun <T : Any> T?.errorOnNull(errorMessage: String): T {
    return this ?: throw QueueElementException(errorMessage)
}