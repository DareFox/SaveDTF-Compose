package shared.document.operations

import kmtt.models.entry.Entry
import shared.document.AbstractProcessorOperation
import org.jsoup.nodes.Document
import shared.i18n.Lang

/**
 * Remove all advertisements from document
 */
object RemoveAdsOperation : AbstractProcessorOperation() {
    override val name: String
        get() = Lang.cssRemoveOperation

    override suspend fun process(arguments: OperationArguments): Document {
        val entry = arguments.entry
        val document = arguments.document

        withProgressSuspend("Removing ads from document") {
            document.getElementsByClass("propaganda").forEach {
                it.remove()
            }
        }

        clearProgress()
        return document
    }
}