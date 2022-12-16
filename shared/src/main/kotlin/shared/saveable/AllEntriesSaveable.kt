package shared.saveable

import exception.errorOnNull
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kmtt.models.enums.Website
import kotlinx.coroutines.runBlocking
import shared.ktor.HttpClient
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import shared.document.IProcessorOperation
import shared.i18n.Lang
import java.io.File

interface IAllEntriesSaveable : ISaveable {
    val site: Website
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}

open class AllEntriesSaveable(
    override val site: Website,
    apiTimeoutInSeconds: Int,
    entryTimeoutInSeconds: Int,
    operations: Set<IProcessorOperation>,
    folderToSave: File,
    httpClient: HttpClient
) : AbstractSaveable(
    emptyLambda = {},
    apiTimeoutInSeconds = apiTimeoutInSeconds,
    entryTimeoutInSeconds = entryTimeoutInSeconds,
    operations = operations,
    folderToSave = folderToSave,
    httpClient = httpClient
), IAllEntriesSaveable {
    private var sitemapDoc: Document? = null
    protected val yearRegex = "/year-\\d{4}-\\d{2}-\\d{2}".toRegex(RegexOption.IGNORE_CASE)

    override suspend fun initializeImpl() {
        setProgress(Lang.allEntriesVmFetchingSitemap)

        val sitemap = "https://${site.baseURL}/sitemap"
        val response = httpClient.rateRequest<HttpResponse> {
            url(sitemap)
        }

        setProgress(Lang.allEntriesVmParsingSitemap)
        sitemapDoc = Jsoup.parse(response.readText())
    }

    override suspend fun saveImpl() {
        var counter = 0
        val parentDir = baseSaveFolder.resolve("$site/entry")
        val errorList = mutableListOf<String>()

        val sequence = sitemapDoc?.let {
            sequenceOfPages(it)
        }.errorOnNull("Sitemap document is null")


        setProgress(Lang.profileElementVmAllEntriesMessage)

        sequence.forEach {
            if (!tryProcessEntry(it, parentDir, counter)) {
                errorList += it
            }
            counter++
            setProgress(Lang.allEntriesVmWaiting.format(counter, errorList.count()))
        }

        showResult(errorList, counter, parentDir.absolutePath)
    }


    protected fun sequenceOfPages(sitemapDoc: Document): Sequence<String> {
        val sitemap = sitemapDoc.selectFirst("ul.sitemap")

        requireNotNull(sitemap) {
            "Can't get sitemap list by ul.sitemap css query"
        }

        val yearPages = sitemap.children().mapNotNull {
            val anchor = it.selectFirst("a") ?: return@mapNotNull null
            val link = anchor.attr("href")

            if (link.isBlank()) return@mapNotNull null

            val yearRegex = "/year-".toRegex(RegexOption.IGNORE_CASE)

            if (link.contains(yearRegex)) {
                link
            } else {
                null
            }
        }

        return sequence<String> {
            val failedLinks = mutableListOf<String>()

            for (it in yearPages) {
                setProgress(Lang.allEntriesVmFetchingYearLink.format(it))

                try {
                    val response = runBlocking {
                        httpClient.rateRequest<HttpResponse> {
                            url(it)
                        }
                    }

                    val doc = runBlocking {
                        Jsoup.parse(response.readText())
                    }

                    val links = convertYearPageToList(doc)

                    yieldAll(links)
                } catch (ex: Exception) {
                    logger.error(ex) {
                        "Caught error during parsing $it in sequence. $it period will be skipped silently!!!!!"
                    }
                    failedLinks += it
                }
            }

            if (failedLinks.isNotEmpty()) {
                val links = failedLinks.joinToString("\n")
                logger.error("FAILED TO DOWNLOAD THIS PERIOD LINKS:\n$links")
            }
        }
    }

    protected fun convertYearPageToList(yearPage: Document): List<String> {
        setProgress(Lang.allEntriesVmParsingAllLinks)
        val sitemap = yearPage.selectFirst("ul.sitemap")

        requireNotNull(sitemap) {
            "Can't get sitemap list by ul.sitemap css query"
        }

        val entryLinks = sitemap.children().mapNotNull {
            val anchor = it.selectFirst("a") ?: return@mapNotNull null
            val link = anchor.attr("href")

            link.ifBlank { null }
        }

        return entryLinks
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AllEntriesSaveable

        if (site != other.site) return false
        if (sitemapDoc != other.sitemapDoc) return false
        if (yearRegex != other.yearRegex) return false

        return true
    }

    override fun hashCode(): Int {
        var result = site.hashCode()
        result = 31 * result + (sitemapDoc?.hashCode() ?: 0)
        result = 31 * result + yearRegex.hashCode()
        return result
    }
}
