package shared.document.operations

import kmtt.models.entry.Entry
import kotlinx.coroutines.yield
import shared.document.AbstractProcessorOperation
import org.jsoup.nodes.Document
import shared.i18n.Lang
import java.io.File

/**
 * Save HTML file
 */
class SaveHtmlFileOperation(val filename: String = "index.html") : AbstractProcessorOperation() {
    override val name: String
        get() = Lang.saveHtmlOperation

    override suspend fun process(arguments: OperationArguments): Document {
        val document = arguments.document
        val saveFolder = arguments.saveFolder

        withProgressSuspend(Lang.savingIndexHtml) {
            // Create file and path of directories to it
            val indexHTML = saveFolder.mkdirs().let { saveFolder.resolve(filename) }

            yield()

            indexHTML.writeBytes(document.toString().toByteArray())
        }

        clearProgress()
        return document
    }
}