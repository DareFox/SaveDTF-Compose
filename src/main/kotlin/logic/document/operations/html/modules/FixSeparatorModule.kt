package logic.document.operations.html.modules

import org.jsoup.nodes.Document

object FixSeparatorModule : IHtmlFormatModule {
    override fun process(document: Document): Document {
        document.getElementsByClass("block-delimiter").forEach {
            it.text("***")
        }

        return document
    }
}