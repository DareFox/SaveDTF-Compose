package logic.document.operations.format.modules

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import util.dom.recreateWithoutNodes

object FormatPersonBlockModule : IHtmlFormatModule {
    override suspend fun process(document: Document): Document {
        val personBlocks = document.getElementsByClass("block-person__image")
        personBlocks.forEach {
            it.removeAttr("style")
        }

        return document
    }
}