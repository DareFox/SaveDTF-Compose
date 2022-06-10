package logic.document.operations

import logic.document.AbstractProcessorOperation
import org.jsoup.nodes.Document

/**
 * Remove all CSS style tags from document
 */
object RemoveCssOperation : AbstractProcessorOperation() {
    override val name: String = "CSS Remover"

    override suspend fun process(document: Document): Document {
        document.getElementsByTag("style").forEachIndexed { index, cssElement ->
            withProgress("Removed $index style tag") {
                cssElement.remove()
            }
        }

        clearProgress()
        return document
    }
}