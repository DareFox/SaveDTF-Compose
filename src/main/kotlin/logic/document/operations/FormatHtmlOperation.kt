package logic.document.operations

import logic.document.AbstractProcessorOperation
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import util.filesystem.readResource

/**
 * Take given document with its content and place it in HTML template wrapper
 */
object FormatHtmlOperation : AbstractProcessorOperation() {
    override val name: String = "HTML Reformater"

    override suspend fun process(document: Document): Document {
        val template = withProgressSuspend("Reading html template") {
            readResource("templates/entry.html").readText()
        }

        val templateDocument = withProgressSuspend("Parsing html template") {
            Jsoup.parse(template)
        }

        val wrapper = withProgressSuspend("Getting wrapper") {
            templateDocument
                .getElementsByClass("savedtf-insert-here")
                .first()
        }

        requireNotNull(wrapper) {
            "There's no wrapper in html template"
        }

        withProgressSuspend("Combining template with document") {
            wrapper.appendChild(document.body())
        }

        return templateDocument
    }
}