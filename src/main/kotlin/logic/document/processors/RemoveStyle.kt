package logic.document.processors

import org.jsoup.nodes.Document

fun Document.removeStyles(): Document {
    this.getElementsByTag("style").forEach {
        it.remove()
    }

    return this
}