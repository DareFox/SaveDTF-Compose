package util.dom

import kmtt.models.enums.Website
import org.jsoup.nodes.Document
import util.kmttapi.UrlUtil

fun Document.getWebsite(): Website? {
    val header = getElementsByClass("content-header-author").first() ?: return null
    val url = header.attr("href")

    return UrlUtil.getWebsiteType(url)
}