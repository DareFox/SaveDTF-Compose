package logic.document.operations

import logic.document.AbstractProcessorOperation
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import util.filesystem.readResource

object JavascriptAndCssOperation: AbstractProcessorOperation() {
    override val name: String
        get() = "JS & CSS"

    override suspend fun process(document: Document): Document {
        val wrapper = document
            .getElementsByClass("savedtf-insert-here")
            .first()

        requireNotNull(wrapper)

        val galleryJS = readResource("templates/index.js").readText()
        val css = readResource("templates/style.css").readText()
        val galleryModal = readResource("templates/galleryModal.html").readText().let { Jsoup.parse(it) }
        val commentsCss = readResource("templates/comments.css").readText()
        val commentsJs = readResource("templates/comments.js").readText()


        wrapper
            .appendChild(galleryModal.body())
            .appendChild(Element("style").also {
                it.html(css)
            })
            .appendChild(Element("style").also {
                it.html(commentsCss)
            })
            // Important: Add JS as last element
            .appendChild(Element("script").also {
                it.html(galleryJS)
            })
            .appendChild(Element("script").also {
                it.html(commentsJs)
            })

        return document
    }
}