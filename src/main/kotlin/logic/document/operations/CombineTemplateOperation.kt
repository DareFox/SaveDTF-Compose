package logic.document.operations

import logic.document.AbstractProcessorOperation
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import ui.i18n.Lang
import util.filesystem.readResource

object CombineTemplateOperation: AbstractProcessorOperation() {
    override val name: String
        get() = "Template"

    override suspend fun process(document: Document): Document {
        val template = withProgressSuspend(Lang.value.readingHtmlTemplate) {
            readResource("templates/entry.html").readText()
        }
        val templateDocument = withProgressSuspend(Lang.value.parsingHtmlTemplate) {
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