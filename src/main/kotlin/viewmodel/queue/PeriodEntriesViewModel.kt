package viewmodel.queue

import exception.errorOnNull
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kmtt.models.enums.Website
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.sync.withLock
import logic.ktor.Client
import logic.ktor.rateRequest
import mu.KotlinLogging
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import ui.i18n.Lang
import viewmodel.SettingsViewModel
import java.io.File

interface IPeriodEntriesViewModel: IQueueElementViewModel {
    val periodSitemapLink: String
    val site: Website
}

class PeriodEntriesViewModel(
    override val periodSitemapLink: String,
    override val site: Website
): AllEntriesViewModel(site), IPeriodEntriesViewModel {
    private var sitemapPeriodDoc: Document? = null

    override suspend fun initializeImpl() {
        val period = yearRegex.find(periodSitemapLink).errorOnNull(
            "Invalid sitemap period"
        )

        setProgress(Lang.value.allEntriesVmFetchingSitemap)

        val response = Client.rateRequest<HttpResponse> {
            url(periodSitemapLink)
        }

        setProgress(Lang.value.allEntriesVmParsingSitemap)
        sitemapPeriodDoc = Jsoup.parse(response.readText())
    }

    override suspend fun saveImpl() {
        var counter = 0
        val errorLinks = mutableListOf<String>()


        val parentDir = File(baseSaveFolder, "${site.name}/entry")
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

        other as PeriodEntriesViewModel

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