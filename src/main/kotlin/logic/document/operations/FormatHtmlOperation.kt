package logic.document.operations

import logic.document.AbstractProcessorOperation
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import ui.viewmodel.SettingsViewModel
import util.filesystem.readResource

/**
 * Take given document with its content and place it in HTML template wrapper
 */
object FormatHtmlOperation : AbstractProcessorOperation() {
    private val lang = SettingsViewModel.proxyLocale

    override val name: String
        get() = lang.value.htmlFormatOperation

    override suspend fun process(document: Document): Document {
        val template = withProgressSuspend(lang.value.readingHtmlTemplate) {
            readResource("templates/entry.html").readText()
        }

        val templateDocument = withProgressSuspend(lang.value.parsingHtmlTemplate) {
            Jsoup.parse(template)
        }

        val wrapper = withProgressSuspend(lang.value.gettingWrapper) {
            templateDocument
                .getElementsByClass("savedtf-insert-here")
                .first()
        }

        requireNotNull(wrapper) {
            lang.value.noWrapperError
        }

        withProgressSuspend(lang.value.combineDocument) {
            wrapper.appendChild(document.body())
        }

        return templateDocument
    }
}