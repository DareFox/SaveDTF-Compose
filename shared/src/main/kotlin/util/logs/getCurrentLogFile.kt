package util.logs

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.core.FileAppender
import org.slf4j.LoggerFactory
import java.io.File

fun getCurrentLogFile(): File? {
    var path: String? = null
    val userDir = System.getProperty("user.dir") ?: return null
    val context = LoggerFactory.getILoggerFactory()

    if (context is LoggerContext) {
        for (logger in context.loggerList) {
            var appenderFile: String? = null
            for (appender in logger.iteratorForAppenders()) {
                if (appender is FileAppender && appender.file != null) {
                    appenderFile = appender.file
                    break
                }
            }

            if (appenderFile != null) {
                path = appenderFile
                break
            }
        }
    }

    if (path == null) return null

    return File(userDir).resolve(path)
}