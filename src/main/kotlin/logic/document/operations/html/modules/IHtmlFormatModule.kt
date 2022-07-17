package logic.document.operations.html.modules

import org.jsoup.nodes.Document

interface IHtmlFormatModule {
    fun process(document: Document): Document
}