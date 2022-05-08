package util

import org.jsoup.nodes.Element

/**
 * Remove child nodes by re-creating element with all it's attributes
 */
fun Element.removeChildNodes(): Element {
    val attributes = this.attributes()
    val classNames = this.classNames()
    val id = this.id()
    val tag = this.tag()

    val newElement = Element(tag, null, attributes)
        .id(id)
        .classNames(classNames)

    this.replaceWith(newElement)

    return newElement
}