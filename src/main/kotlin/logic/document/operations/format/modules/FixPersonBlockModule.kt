package logic.document.operations.format.modules

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import util.dom.recreateWithoutNodes

object FixPersonBlockModule : IHtmlFormatModule {
    override suspend fun process(document: Document): Document {
        val personBlocks = document.getElementsByClass("block-person")
        personBlocks.forEach {
            processBlock(it)
        }

        return document
    }

    /**
     *  Person block is div with `block-person` class
     */
    private fun processBlock(element: Element) {
        val image = element.getElementsByClass("block-person__image")
        image.forEach {
            processImage(it)
        }

        val title = element.getElementsByClass("block-person__title")
        title.forEach {
            processTitle(it)
        }

        val description = element.getElementsByClass("block-person__description")
        description.forEach {
            processDescription(it)
        }
    }

    /**
     * Image is div with `block-person__image` class
     */
    private fun processImage(element: Element) {
        element.attr("style", "display: flex; justify-content: center; width: 100%;")
        val oldChildren = element.children()

        val newElement = element.recreateWithoutNodes()

        oldChildren.forEach {
            val div = Element("div")

            div.attr("style", "height: 110px; width: 110px; border-radius: 100%; overflow: hidden;")

            div.appendChild(it)
            newElement.appendChild(div)
        }
    }

    /**
     * Title is div with `block-person__title` class
     */
    private fun processTitle(element: Element) {
        element.attr("style", "text-align: center;")
    }

    /**
     * Description is div with `block-person__description` class
     */
    private fun processDescription(element: Element) {
        processTitle(element)
    }
}