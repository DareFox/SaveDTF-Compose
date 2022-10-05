package shared.util.dom

import kmtt.models.enums.Website
import org.jsoup.nodes.Document
import shared.util.kmttapi.KmttUrl

fun Document.getWebsite(): Website? {
    val header = getElementsByClass("content-header-author").first() ?: return null
    val url = header.attr("href")

    return KmttUrl.getWebsiteType(url)
}