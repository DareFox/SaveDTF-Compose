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

        requireNotNull(wrapper) {
            lang.value.noWrapperError
        }

        withProgressSuspend("Combining template with document") {
            connectJsAndCss(document, wrapper)
        }

        withProgressSuspend("Format separators") {
            fixSeparators(wrapper)
        }

        return templateDocument
    }
}

private fun fixSeparators(wrapper: Element) {
    wrapper.getElementsByClass("block-delimiter").forEach {
        it.text("***")
    }
}
private fun connectJsAndCss(kmttDocument: Document, wrapper: Element) {
    val javascript = readResource("templates/index.js").readText()

    val css = readResource("templates/style.css").readText()

    val galleryModal = readResource("templates/galleryModal.html").readText().let { Jsoup.parse(it) }

    wrapper
        .appendChild(kmttDocument.body())
        .appendChild(galleryModal.body())
        .appendChild(Element("style").also {
            it.html(css)
        })
        // Important: Add JS as last element
        .appendChild(Element("script").also {
            it.html(javascript)
        })
}