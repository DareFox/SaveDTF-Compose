package logic.document.operations

import kmtt.models.entry.Entry
import kmtt.models.enums.SortingType
import kmtt.util.CommentNode
import kmtt.util.toTree
import logic.document.AbstractProcessorOperation
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import util.dom.getWebsite
import util.filesystem.readResource
import util.kmttapi.SharedRegex
import util.kmttapi.betterPublicKmtt
import util.random.randomColor

class SaveCommentsOperation(val entry: Entry): AbstractProcessorOperation() {
    override val name: String = "Save comments"

    override suspend fun process(document: Document): Document {
        val website = document.getWebsite() ?: return document
        val id = entry.id ?: return document
        val api = betterPublicKmtt(website)

        var currentLayer = api.comments
            .getEntryComments(id, SortingType.POPULAR)
            .toTree()
            .associateWith {
            createNodeHTML(it, null)
        }
        val toAdd = currentLayer.values

        while (currentLayer.isNotEmpty()) {
            val nextLayer = mutableMapOf<CommentNode, Element>()

            for ((node, element) in currentLayer) {
                val nodesDiv = element
                    .select(".comment-node .nodes")
                    .first() ?: throw IllegalArgumentException("No nodes element")

                val randomColor = randomColor()

                val htmlChildren = node.children.associateWith {
                    val htmlNode = createNodeHTML(it, "#${randomColor.toHex()}")
                    nodesDiv.appendChild(htmlNode)
                    htmlNode
                }

                nextLayer += htmlChildren
            }

            currentLayer = nextLayer
        }

        val wrapper = document
            .getElementsByClass("savedtf-wrapper")
            .first() ?: throw IllegalArgumentException("No body wrapper")

        wrapper.appendChild(Element("div").apply {
            addClass("comments-list")
            appendChildren(toAdd)
        })

        return document
    }

    private fun createNodeHTML(comment: CommentNode, hideColor: String?): Element {
        // Parse comment node template from resources folder
        val commentNode = readResource("templates/comment.html")
            .readText()
            .let { Jsoup.parse(it) }
            .body()
            .children()
            .first() ?: throw IllegalArgumentException("No template of comment-node")

        // Get avatar
        val avatar = commentNode
            .select(".comment .header .avatar")
            .first() ?: throw IllegalArgumentException("No avatar element")

        // Set avatar url
        avatar.attr("src", comment.value.author?.avatarUrl ?: "")

        // Get nickname div
        val nickname = commentNode
            .select(".comment .header .nickname")
            .first() ?: throw IllegalArgumentException("No nickname element")

        // Set nickname
        nickname.text(comment.value.author?.name ?: "Unknown")

        // Get karma div
        val karma = commentNode
            .select(".comment .karma")
            .first() ?: throw IllegalArgumentException("No karma element")

        // Set karma value
        val voteSummary = comment.value.likes?.summ
        if (voteSummary != null) {
            val karmaType = when {
                voteSummary > 0 -> "positive-karma"
                voteSummary < 0 -> "negative-karma"
                else -> "neutral-karma"
            }

            karma.addClass(karmaType)
            karma.text(voteSummary.toString())
        }

        // Get content block
        val commentText = commentNode
            .select(".comment .content .text")
            .first() ?: throw IllegalArgumentException("No content element")

        commentText.text(comment.value.text ?: "")
        makeElementTextLinkable(commentText)

        // Get/create attachment block
        val attachment = commentNode
            .select(".comment .content .attachment")
            .first() ?: Element("div").apply {
                addClass("attachment")
                commentText.appendChild(this)
            }

        comment.value.media?.forEach {
            val iframeURL = it.iframeUrl

            if (iframeURL != null) {
                val wrapper = Element("div").apply {
                    addClass("andropov_video--iframe")
                }

                val iframe = Element("iframe").apply {
                    attr("src", iframeURL)
                    attr( // copied from embed generated by YouTube
                        "allow",
                        "accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                    )
                    attr("allowfullscreen", "")
                    attr("frameborder", "0")
                    attr("width", "100%")
                    attr("height", "100%")
                    attr("title", "Embed video player")
                    wrapper.appendChild(this)
                }

                attachment.appendChild(wrapper)
                return@forEach
            }

            // If media is video, then Media.type will return IMAGE type.
            // So to get real type we need to check additionalData field
            val realType = it.additionalData?.type
            if (realType in listOf("gif", "mp4")) {
                val videoPreviewElement = Element("video").apply {
                    attr("autoplay", "")
                    attr("muted", "")
                    attr("loop", "")
                    attr("controls", "")
                    addClass("gall-vid-preview")
                    attr("src", it.additionalData?.url ?: "")
                }

                attachment.appendChild(videoPreviewElement)
                return@forEach
            }

            val url = it.additionalData?.url ?: return@forEach

            // Else it's image
            val imageDiv = Element("div").apply {
                addClass("andropov_image andropov_image--comment")
                attr("data-image-src", url)

                val image = Element("img")
                appendChild(image)
            }
            attachment.appendChild(imageDiv)
        }

        // Check nodes block
        val nodes = commentNode
            .select(".comment-node .nodes")
            .first() ?: throw IllegalArgumentException("No nodes element")

        // Get hide block
        val hide = commentNode
            .select(".comment-node .hide")
            .first() ?: throw IllegalArgumentException("No hide element")

        // Set color of hide block
        if (hideColor != null) {
            hide.attr("style", "background: $hideColor" )
        } else {
            hide.addClass("hide--disabled")
        }

        // Get date block
        val date = commentNode
            .select(".comment .info .date")
            .first() ?: throw IllegalArgumentException("No date element")

        // Set date
        val unixTime = comment.value.date ?: ""
        date.attr("unix-time", unixTime.toString())

        return commentNode
    }

    private fun makeElementTextLinkable(element: Element) {
        val text = element.text()
        val newText = text.replace(SharedRegex.urlRegex) {
            "<a href=\"${it.value}\">${it.value}</a>"
        }
        element.html(newText)
    }
}