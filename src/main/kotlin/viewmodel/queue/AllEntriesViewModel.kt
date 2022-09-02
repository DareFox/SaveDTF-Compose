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

class AllEntriesViewModel(val site: Website): AbstractElementViewModel() {
    private var sitemapDoc: Document? = null
    private val logger = KotlinLogging.logger { }

    override suspend fun initialize() {
        progress("Fetching sitemap")

        val sitemap = "https://${site.baseURL}/sitemap"
        val response = Client.rateRequest<HttpResponse> {
            url(sitemap)
        }

        progress("Parsing sitemap")
        sitemapDoc = Jsoup.parse(response.readText())

        clearProgress()
        readyToUse()
    }

    override suspend fun save(): Deferred<Boolean> {
        val progress = this
        return waitAndAsyncJob {
            val sequence = sitemapDoc?.let {
                sequenceOfPages(it)
            }.errorOnNull("Sitemap document is null")

            var errorCounter = 0
            var counter = 0
            val allEntriesMessage = Lang.value.profileElementVmAllEntriesMessage

            elementMutex.withLock {
                inUse()
                withProgressSuspend(allEntriesMessage) {
                    sequence.forEach {
                        if (!tryProcessDocument(it, progress, counter)) {
                            errorCounter++
                        }
                        counter++
                        progress("Waiting next entry. Finished: $counter. Failed: $errorCounter.")
                    }
                }

                if (errorCounter > 0) {
                    saved()
                    progress(Lang.value.profileElementVmSomeErrors.format(counter, errorCounter))
                } else if (errorCounter == counter) {
                    error(Lang.value.profileElementVmAllErrors.format(errorCounter))
                    clearProgress()
                } else {
                    saved()
                    progress(Lang.value.profileElementVmNoErrors.format(counter))
                }
            }

            errorCounter > 0
        }
    }

    private suspend fun tryProcessDocument(url: String, progress: IProgress, currentCounter: Int): Boolean {
        return try {
            val entry = betterPublicKmtt(site).entry.getEntry(url)

            val document = entry
                .entryContent
                .errorOnNull("Entry content is null")
                .html
                .errorOnNull("Entry html is null")
                .let { Jsoup.parse(it) } // parse document

            val processor = SettingsBasedDocumentProcessor(entry.toDirectory(File(pathToSave)), document, entry)

            processor
                .redirectTo(mutableProgress, ioScope) {// redirect progress of processor to this VM progress
                    val progressValue = it?.run { ", $this" } ?: ""

                    // show entry counter
                    if (currentJob.value?.isCancelled != true) "${Lang.value.queueVmEntry} #${currentCounter + 1}$progressValue"
                    // show nothing on cancellation
                    else null
                }
                .cancelOnSuspendEnd {
                    progress("Processing entry")
                    processor.process() // save document
                }


            true
        } catch (ex: Exception) { // on error, change result to false
            logger.error(ex) {"Error during processing document $url"}
            false
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
                progress("Requesting all entries from $it")

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
            }
        }
    }

    private fun convertYearPageToList(yearPage: Document): List<String> {
        progress("Parsing all links")
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
}