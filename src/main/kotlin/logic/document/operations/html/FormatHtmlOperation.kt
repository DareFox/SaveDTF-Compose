package logic.document.operations.html

import logic.document.AbstractProcessorOperation
import logic.document.operations.html.modules.FixSeparatorModule
import logic.document.operations.html.modules.IHtmlFormatModule
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
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

        var baseDocument = combineWithTemplate(document, templateDocument)

        val operations = listOf<IHtmlFormatModule>(
            FixSeparatorModule
        )

        operations.forEach {
            baseDocument = it.process(baseDocument)
        }

        return baseDocument
    }

    private fun combineWithTemplate(kmttDocument: Document, templateDocument: Document): Document {
        val wrapper = templateDocument
            .getElementsByClass("savedtf-insert-here")
            .first()

        requireNotNull(wrapper)

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

        return templateDocument
    }
}

