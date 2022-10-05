package shared.document.operations

import shared.document.AbstractProcessorOperation
import org.jsoup.nodes.Document
import shared.i18n.Lang

/**
 * Remove all advertisements from document
 */
object RemoveAdsOperation : AbstractProcessorOperation() {
    override val name: String
        get() = Lang.cssRemoveOperation

    override suspend fun process(document: Document): Document {
        withProgressSuspend("Removing ads from document") {
            document.getElementsByClass("propaganda").forEach {
                it.remove()
            }
        }

        clearProgress()
        return document
    }
}