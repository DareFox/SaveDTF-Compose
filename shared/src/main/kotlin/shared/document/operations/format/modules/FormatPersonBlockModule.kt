package shared.document.operations.format.modules

import org.jsoup.nodes.Document

object FormatPersonBlockModule : IHtmlFormatModule {
    override suspend fun process(document: Document): Document {
        val personBlocks = document.getElementsByClass("block-person__image")
        personBlocks.forEach {
            it.removeAttr("style")
        }

        return document
    }
}