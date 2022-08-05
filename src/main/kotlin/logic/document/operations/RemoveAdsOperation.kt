package logic.document.operations

import logic.document.AbstractProcessorOperation
import org.jsoup.nodes.Document
import ui.i18n.Lang
import ui.viewmodel.SettingsViewModel

/**
 * Remove all advertisements from document
 */
object RemoveAdsOperation : AbstractProcessorOperation() {
    override val name: String
        get() = Lang.value.cssRemoveOperation

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