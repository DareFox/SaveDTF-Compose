package shared.document.operations


import kmtt.models.entry.Entry

import shared.document.AbstractProcessorOperation
import org.jsoup.nodes.Document
import shared.i18n.Lang

/**
 * Change title to SaveDTF
 */
class ChangeTitleOperation() : AbstractProcessorOperation() {
    override val name: String
        get() = Lang.titleOperation

    override suspend fun process(document: Document, entry: Entry?): Document {
        withProgress(Lang.titleOperationProgress) {
            document.head().getElementsByTag("title").first()?.text("SaveDTF")
            // If title is not null, then use entry title as html title,
            // Else, if author is not null -> use author and subsite name
            // Else show "No title"
            val title =
                entry?.title ?: entry?.author?.name?.let { "Entry by $it in ${entry.subsite?.name}" } ?: "No title"

            document.head().getElementsByTag("title").first()?.text("$title [SaveDTF]")

        }

        clearProgress()
        return document
    }
}