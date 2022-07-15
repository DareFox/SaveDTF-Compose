package logic.document.operations


import kmtt.models.entry.Entry

import logic.document.AbstractProcessorOperation
import logic.document.operations.FormatHtmlOperation
import org.jsoup.nodes.Document
import ui.i18n.Lang
import ui.viewmodel.SettingsViewModel

/**
 * Change title to SaveDTF
 */
class ChangeTitleOperation(val entry: Entry? = null) : AbstractProcessorOperation() {
    override val name: String
        get() = Lang.value.titleOperation

    override suspend fun process(document: Document): Document {
        withProgress(Lang.value.titleOperationProgress) {
            document.head().getElementsByTag("title").first()?.text("SaveDTF")
            // If title is not null, then use entry title as html title,
            // Else, if author is not null -> use author and subsite name
            // Else show "No title"
            val title = entry?.title ?: entry?.author?.name?.let { "Entry by $it in ${entry.subsite?.name}" } ?: "No title"

            document.head().getElementsByTag("title").first()?.text("$title [SaveDTF]")

        }

        clearProgress()
        return document
    }
}