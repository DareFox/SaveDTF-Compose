package shared.document.operations

import shared.document.AbstractProcessorOperation
import org.jsoup.nodes.Document
import shared.i18n.Lang

/**
 * Remove all CSS style tags from document
 */
object RemoveCssOperation : AbstractProcessorOperation() {
    override val name: String
        get() = Lang.cssRemoveOperation

    override suspend fun process(document: Document): Document {
        document.getElementsByTag("style").forEachIndexed { index, cssElement ->
            withProgress(Lang.removedStyleTag.format(index)) {
                cssElement.remove()
            }
        }

        clearProgress()
        return document
    }
}