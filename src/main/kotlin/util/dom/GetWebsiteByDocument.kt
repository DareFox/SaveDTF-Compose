package util.dom

import kmtt.models.enums.Website
import org.jsoup.nodes.Document
import util.kmttapi.KmttUrl

fun Document.getWebsite(): Website? {
    val header = getElementsByClass("content-header-author__name")
    val url = header.firstNotNullOfOrNull {
        it.attr("href").ifEmpty { null }
    } ?: return null

    return KmttUrl.getWebsiteType(url)
}