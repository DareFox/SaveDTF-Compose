package logic.document.operations.format

import logic.document.AbstractProcessorOperation
import logic.document.operations.format.modules.FixPersonBlockModule
import logic.document.operations.format.modules.FixSeparatorModule
import logic.document.operations.format.modules.IHtmlFormatModule
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
        var result = document

        val operations = listOf<IHtmlFormatModule>(
            FixSeparatorModule,
            FixPersonBlockModule
        )

        operations.forEach {
            result = it.process(result)
        }

        return result
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

