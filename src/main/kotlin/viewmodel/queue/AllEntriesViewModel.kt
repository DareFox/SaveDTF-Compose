package viewmodel.queue

import exception.errorOnNull
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kmtt.models.enums.Website
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.withLock
import logic.abstracts.IProgress
import logic.document.SettingsBasedDocumentProcessor
import logic.ktor.Client
import logic.ktor.rateRequest
import mu.KotlinLogging
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import ui.i18n.Lang
import util.coroutine.cancelOnSuspendEnd
import util.filesystem.toDirectory
import util.kmttapi.betterPublicKmtt
import util.progress.redirectTo
import java.io.File

interface IAllEntriesViewModel: IQueueElementViewModel {
    val site: Website
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}

class AllEntriesViewModel(override val site: Website): AbstractElementViewModel(), IAllEntriesViewModel {
    private var sitemapDoc: Document? = null
    private val logger = KotlinLogging.logger { }
    private val parentDir = File(pathToSave, "${site.name}/entry")

    override suspend fun initialize() {
        elementMutex.withLock {
            try {
                initializing()
                progress(Lang.value.allEntriesVmFetchingSitemap)

                val sitemap = "https://${site.baseURL}/sitemap"
                val response = Client.rateRequest<HttpResponse> {
                    url(sitemap)
                }

                progress(Lang.value.allEntriesVmParsingSitemap)
                sitemapDoc = Jsoup.parse(response.readText())

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
                inUse()

                val sequence = sitemapDoc?.let {
                    sequenceOfPages(it)
                }.errorOnNull("Sitemap document is null")

                var counter = 0
                val allEntriesMessage = Lang.value.profileElementVmAllEntriesMessage

                val errorList = mutableListOf<String>()
                withProgressSuspend(allEntriesMessage) {
                    sequence.forEach {
                        if (!tryProcessDocument(it, parentDir, counter, logger)) {
                            errorList += it
                        }
                        counter++
                        progress(Lang.value.allEntriesVmWaiting.format(counter, errorList.count()))
                    }
                }

                val errorCounter = errorList.count()
                if (errorCounter > 0) {
                    saved()
                    progress(Lang.value.profileElementVmSomeErrors.format(counter, errorCounter))
                    logger.error("Failed to download:\n${errorList.joinToString(",\n")}")
                } else if (errorCounter == counter) {
                    error(Lang.value.profileElementVmAllErrors.format(errorCounter))
                    logger.error("Failed to download:\n${errorList.joinToString(",\n")}")
                    clearProgress()
                } else {
                    saved()
                    progress(Lang.value.profileElementVmNoErrors.format(counter))
                }

                errorCounter == 0
            }
        }
    }

    private fun sequenceOfPages(sitemapDoc: Document): Sequence<String> {
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
            for (it in yearPages) {
                progress(Lang.value.allEntriesVmFetchingYearLink.format(it))

                try {
                    val response = runBlocking {
                        Client.rateRequest<HttpResponse> {
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
                        "Caught error during parsing $it in sequence"
                    }
                }
            }
        }
    }

    private fun convertYearPageToList(yearPage: Document): List<String> {
        progress(Lang.value.allEntriesVmParsingAllLinks)
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

        other as AllEntriesViewModel

        if (site != other.site) return false

        return true
    }

    override fun hashCode(): Int {
        return site.hashCode()
    }
}