package shared.document.operations

import kmtt.models.entry.Entry
import shared.document.AbstractProcessorOperation
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import shared.i18n.Lang
import shared.util.filesystem.readResource

object CombineTemplateOperation : AbstractProcessorOperation() {
    override val name: String
        get() = Lang.templateHtmlOperation

    override suspend fun process(document: Document, entry: Entry?): Document {
        val template = withProgressSuspend(Lang.readingHtmlTemplate) {
            readResource("templates/entry.html").readText()
        }
        val templateDocument = withProgressSuspend(Lang.parsingHtmlTemplate) {
            Jsoup.parse(template)
        }

        val wrapper = templateDocument
            .getElementsByClass("savedtf-insert-here")
            .first()

        requireNotNull(wrapper)

        wrapper.appendChild(document.body())

        return templateDocument
    }
}