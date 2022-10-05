package util.dom

import org.jsoup.nodes.Element

/**
 * Remove child nodes by re-creating element with all it's attributes
 *
 * **WARNING: Old element will be linked to void, so reference to the new one**
 */
fun Element.recreateWithoutNodes(): Element {
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