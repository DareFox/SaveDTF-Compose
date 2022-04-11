package logic.document.processors

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import util.readResource

fun Document.reformat(): Document {
    val template = readResource("templates/entry.html").readText()

    val templateDocument = Jsoup.parse(template)

    templateDocument
        .getElementsByClass("savedtf-insert-here")
        .first()!!
        .appendChild(this.body())

    return templateDocument
}