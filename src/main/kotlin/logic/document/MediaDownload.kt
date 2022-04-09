package logic.document

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.coroutines.*
import logic.ktor.Client
import logic.ktor.rateRequest
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

suspend fun Document.saveMedia(): Document {
    val imageContainers = getElementsByClass("andropov_image").filter {
        it.attr("data-image-src").isNotEmpty()
    }

    val responses = downloadElements(imageContainers, "data-image-src")

    responses.forEach { (element, response) ->
        val contentHeader = response.contentType()!!

        // Wrap content
        val binaryMedia = BinaryMedia(
            type = contentHeader.contentType,
            subtype = contentHeader.contentSubtype,
            binary = response.content.toByteArray()
        )

        // Delete all children from Element node
        // I think it could be done faster, if instead of remove children,
        // we will just remove parent node and recreate it with new children
        element.children().forEach {
            it.remove()
        }

        val newElement = Element("img")
            .attr("src", binaryMedia.toBase64HTML())

        element.prependChild(newElement)
    }

    return this
}


private suspend fun downloadElements(elements: List<Element>, attributeMediaURL: String): MutableMap<Element, HttpResponse> {
    val downloaderScope = CoroutineScope(Dispatchers.IO)
    val downloadJobs = mutableListOf<Job>()
    val downloadMap = mutableMapOf<Element, HttpResponse>()

    elements.forEach {
        // Download media concurrently
        downloadJobs += downloaderScope.launch {
            val response: HttpResponse = Client.rateRequest {
                method = HttpMethod.Get
                url(it.attr(attributeMediaURL))
            }
            downloadMap[it] = response
        }
    }

    downloadJobs.joinAll() // Wait all downloads
    return downloadMap
}