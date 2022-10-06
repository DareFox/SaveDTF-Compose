package shared.document.operations.format

import shared.document.AbstractProcessorOperation
import shared.document.operations.format.modules.*
import org.jsoup.nodes.Document
import shared.document.operations.OperationArguments
import shared.i18n.Lang

/**
 * Take given document with its content and place it in HTML template wrapper
 */
object FormatHtmlOperation : AbstractProcessorOperation() {
    override val name: String
        get() = Lang.htmlFormatOperation

    override suspend fun process(arguments: OperationArguments): Document {
        var document = arguments.document

        val operations = listOf<IHtmlFormatModule>(
            FormatSeparatorModule,
            FormatPersonBlockModule,
            FormatQuizModule,
            FormatQuoteModule,
            FormatGalleryModule,
            FormatEmbedModule,
            FormatLinkModule
        )

        operations.forEach {
            document = it.process(document)
        }

        return document
    }
}

