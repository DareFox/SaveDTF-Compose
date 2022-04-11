package logic.document.processors

import org.jsoup.nodes.Document

fun Document.changeTitle(newTitle: String): Document {
    this.head().getElementsByTag("title").first()?.text(newTitle)
    return this
}