package viewmodel.queue

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
import java.io.File

interface IPeriodEntriesViewModel: IQueueElementViewModel {
    val periodSitemapLink: String
    val site: Website
}

class PeriodEntriesViewModel(
    override val periodSitemapLink: String,
    override val site: Website
): AllEntriesViewModel(site), IPeriodEntriesViewModel {
    private val parentDir = File(pathToSave, "${site.name}/entry")
    private var sitemapPeriodDoc: Document? = null
    private val logger = KotlinLogging.logger { }

    override suspend fun initialize() {
        elementMutex.withLock {
            initializing()
            val period = yearRegex.find(periodSitemapLink)

            if (period == null) {
                error("Invalid sitemap period")
                return@withLock
            }

            try {
                progress(Lang.value.allEntriesVmFetchingSitemap)

                val response = Client.rateRequest<HttpResponse> {
                    url(periodSitemapLink)
                }

                progress(Lang.value.allEntriesVmParsingSitemap)
                sitemapPeriodDoc = Jsoup.parse(response.readText())

                clearProgress()
                readyToUse()
            } catch (ex: Exception) {
                error(ex)
            }
        }
    }

    override suspend fun save(): Deferred<Boolean> {
        return waitAndAsyncJob {
            elementMutex.withLock {
                var counter = 0
                val errorLinks = mutableListOf<String>()
                val document = sitemapPeriodDoc
                if (document == null) {
                    error("Document is null")
                    return@withLock false
                }

                val entriesLinks = convertYearPageToList(document)

                if (entriesLinks.isEmpty()) {
                    error("No links in sitemap $periodSitemapLink")
                    return@withLock false
                }

                entriesLinks.forEach {
                    if (!tryProcessDocument(it, parentDir, counter, logger = logger)) {
                        errorLinks += it
                    }
                    counter++
                }

                resultMessage(errorLinks, counter, logger)
            }
        }
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