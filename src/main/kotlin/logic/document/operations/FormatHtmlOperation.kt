package logic.document.operations

import logic.document.AbstractProcessorOperation
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.parser.Tag
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

        val javascript = withProgressSuspend("Getting JS for wrapper") {
            readResource("templates/index.js").readText()
        }

        val css = withProgressSuspend("Getting CSS for wrapper") {
            readResource("templates/style.css").readText()
        }

        val galleryModal = withProgressSuspend("Getting gallery module") {
            val code = readResource("templates/galleryModal.html").readText()
            Jsoup.parse(code)
        }

        requireNotNull(wrapper) {
            lang.value.noWrapperError
        }

        withProgressSuspend("Combining template with document") {
            wrapper
                .appendChild(document.body())
                .appendChild(galleryModal.body())
                .appendChild(Element("style").also {
                    it.html(css)
                })
                // Important: Add JS as last element
                .appendChild(Element("script").also {
                    it.html(javascript)
                })
        }

        withProgressSuspend("Format separators") {
            wrapper.getElementsByClass("block-delimiter").forEach {
                it.text("***")
            }
        }

        return templateDocument
    }
}