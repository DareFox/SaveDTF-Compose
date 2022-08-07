package logic.document.operations

import logic.document.AbstractProcessorOperation
import org.jsoup.nodes.Document
import ui.i18n.Lang

/**
 * Remove all CSS style tags from document
 */
object RemoveCssOperation : AbstractProcessorOperation() {
    override val name: String
        get() = Lang.value.cssRemoveOperation

    override suspend fun process(document: Document): Document {
        document.getElementsByTag("style").forEachIndexed { index, cssElement ->
            withProgress(Lang.value.removedStyleTag.format(index)) {
                cssElement.remove()
            }
        }

        clearProgress()
        return document
    }
}