package logic.document.operations.format

import logic.document.AbstractProcessorOperation
import logic.document.operations.format.modules.*
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
            FixPersonBlockModule,
            FixQuizModule,
            FixQuoteModule,
            FixGalleryModule
        )

        operations.forEach {
            result = it.process(result)
        }

        return result
    }
}

