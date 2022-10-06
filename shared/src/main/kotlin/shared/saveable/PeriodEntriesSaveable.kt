package shared.saveable

import exception.errorOnNull
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kmtt.models.enums.Website
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import shared.document.IDocumentProcessor
import shared.document.IProcessorOperation
import shared.ktor.Client
import shared.ktor.rateRequest
import shared.i18n.Lang
import java.io.File

interface IPeriodEntriesViewModel : ISaveable {
    val periodSitemapLink: String
    val site: Website
}

class PeriodEntriesSaveable(
    override val periodSitemapLink: String,
    override val site: Website,
    apiTimeoutInSeconds: Int,
    entryTimeoutInSeconds: Int,
    operations: Set<IProcessorOperation>,
    folderToSave: File,
) : AllEntriesSaveable(site, apiTimeoutInSeconds, entryTimeoutInSeconds, operations, folderToSave), IPeriodEntriesViewModel {
    private var sitemapPeriodDoc: Document? = null

    override suspend fun initializeImpl() {
        val period = yearRegex.find(periodSitemapLink).errorOnNull(
            "Invalid sitemap period"
        )

        setProgress(Lang.allEntriesVmFetchingSitemap)

        val response = Client.rateRequest<HttpResponse> {
            url(periodSitemapLink)
        }

        setProgress(Lang.allEntriesVmParsingSitemap)
        sitemapPeriodDoc = Jsoup.parse(response.readText())
    }

    override suspend fun saveImpl() {
        var counter = 0
        val errorLinks = mutableListOf<String>()


        val parentDir = baseSaveFolder.resolve("${site.name}/entry")
        val document = sitemapPeriodDoc.errorOnNull("Document is null")

        val entriesLinks = convertYearPageToList(document)

        if (entriesLinks.isEmpty()) {
            throw IllegalArgumentException("No links in sitemap $periodSitemapLink")
        }

        entriesLinks.forEach {
            if (!tryProcessEntry(it, parentDir, counter)) {
                errorLinks += it
            }
            counter++
        }

        showResult(errorLinks, counter, parentDir.absolutePath)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as PeriodEntriesSaveable

        if (periodSitemapLink != other.periodSitemapLink) return false
        if (site != other.site) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + periodSitemapLink.hashCode()
        result = 31 * result + site.hashCode()
        return result
    }
}