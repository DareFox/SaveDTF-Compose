package logic.document.operations

import logic.document.AbstractProcessorOperation
import org.jsoup.nodes.Document

/**
 * Change title to SaveDTF
 */
object ChangeTitleOperation : AbstractProcessorOperation() {
    override val name: String
        get() = "Title"

    override suspend fun process(document: Document): Document {
        withProgress("Changing title") {
            document.head().getElementsByTag("title").first()?.text("SaveDTF")
        }

        clearProgress()
        return document
    }
}