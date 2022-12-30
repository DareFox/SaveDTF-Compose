package util.dom

import kmtt.models.enums.Website
import org.jsoup.nodes.Document
import util.kmttapi.UrlUtil

fun Document.getWebsite(): Website? {
    val header = getElementsByClass("content-header-author__name")
    val url = header.firstNotNullOfOrNull {
        it.attr("href").ifEmpty { null }
    } ?: return null

    return UrlUtil.getWebsiteType(url)
}