package logic.document.operations.format.modules

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

object FormatQuoteModule : IHtmlFormatModule {
    override suspend fun process(document: Document): Document {
        val quotes = document.getElementsByClass("block-quote")
        quotes.forEach {
            removeOldIcon(it)
            addNewIcon(it)
        }

        return document
    }

    private fun addNewIcon(quote: Element?) {
        val quoteIcon = Element("div").also {
            it.addClass("block-quote-icon")
            it.text("❝")
        }

        quote?.insertChildren(0, quoteIcon)
    }

    private fun removeOldIcon(quote: Element?) {
        quote?.getElementsByClass("icon--entry_quote")?.forEach {
            it?.remove()
        }
    }
}