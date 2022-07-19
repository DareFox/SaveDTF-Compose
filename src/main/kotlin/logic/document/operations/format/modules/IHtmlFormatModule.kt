package logic.document.operations.format.modules

import org.jsoup.nodes.Document

interface IHtmlFormatModule {
    suspend fun process(document: Document): Document
}