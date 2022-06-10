package logic.document.operations

import kotlinx.coroutines.yield
import logic.document.AbstractProcessorOperation
import logic.document.IProcessorOperation
import org.jsoup.nodes.Document
import java.io.File

/**
 * Save HTML file
 */
class SaveHtmlFileOperation(val saveFolder: File): AbstractProcessorOperation() {
    override val name: String = "Save HTML File"

    override suspend fun process(document: Document): Document {
        withProgressSuspend("HTML: Saving index.html...") {
            // Create file and path of directories to it
            val indexHTML = saveFolder.mkdirs().let { saveFolder.resolve("index.html") }

            yield()

            indexHTML.writeBytes(document.toString().toByteArray())
        }

        clearProgress()
        return document
    }
}