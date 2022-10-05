package shared.document.operations.format.modules

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import shared.util.dom.recreateWithoutNodes

object FormatLinkModule : IHtmlFormatModule {
    override suspend fun process(document: Document): Document {
        val links = document.getElementsByClass("andropov_link")

        links.forEach { linkContainer ->
            linkContainer.getElementsByTag("svg").remove()
            linkContainer.getElementsByTag("img").remove()

            val url = linkContainer.attr("href")
            if (url.isNullOrEmpty()) return@forEach

            val textWrapper = Element("div").also { wrapper ->
                wrapper.addClass("andropov_link--text_column")
                wrapper.appendChildren(linkContainer.children())
            }

            val icoWrapper = Element("div").also { icoWrapper ->
                icoWrapper.addClass("andropov_link--ico_column")

                val faviconImage = Element("img").also {
                    it.attr("src", "https://www.google.com/s2/favicons?sz=128&domain_url=$url")
                }

                icoWrapper.appendChild(faviconImage)
            }

            val newContainer = linkContainer.recreateWithoutNodes()
            newContainer.appendChild(textWrapper)
            newContainer.appendChild(icoWrapper)
        }

        return document
    }
}