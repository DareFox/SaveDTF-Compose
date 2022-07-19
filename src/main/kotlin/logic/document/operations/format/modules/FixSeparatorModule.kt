package logic.document.operations.format.modules

import org.jsoup.nodes.Document

object FixSeparatorModule : IHtmlFormatModule {
    override suspend fun process(document: Document): Document {
        document.getElementsByClass("block-delimiter").forEach {
            it.text("***")
        }

        return document
    }
}