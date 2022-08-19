package logic.document.operations

import kotlinx.coroutines.yield
import logic.document.AbstractProcessorOperation
import org.jsoup.nodes.Document
import ui.i18n.Lang
import java.io.File

/**
 * Save HTML file
 */
class SaveHtmlFileOperation(val saveFolder: File, val filename: String = "index.html"): AbstractProcessorOperation() {
    override val name: String
        get() = Lang.value.saveHtmlOperation

    override suspend fun process(document: Document): Document {
        withProgressSuspend(Lang.value.savingIndexHtml) {
            // Create file and path of directories to it
            val indexHTML = saveFolder.mkdirs().let { saveFolder.resolve(filename) }

            yield()

            indexHTML.writeBytes(document.toString().toByteArray())
        }

        clearProgress()
        return document
    }
}