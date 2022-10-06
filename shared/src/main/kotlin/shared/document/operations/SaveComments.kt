package shared.document.operations

import kmtt.models.comment.Comment
import kmtt.models.entry.Entry
import kmtt.models.enums.SortingType
import kmtt.util.CommentNode
import kmtt.util.toTree
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.yield
import shared.document.AbstractProcessorOperation
import mu.KotlinLogging
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import shared.i18n.Lang
import shared.util.dom.getWebsite
import shared.util.filesystem.readResource
import shared.util.kmttapi.KmttRegex
import shared.util.kmttapi.betterPublicKmtt
import shared.util.random.RGB
import shared.util.random.offsetRandomColor
import shared.util.random.randomColor

class SaveCommentsOperation : AbstractProcessorOperation() {
    override val name: String
        get() = Lang.saveCommentsOperation
    private val colorRange = 50..255
    private val logger = KotlinLogging.logger { }
    private val offset = 40
    private val maxLayerHideOffset = 10
    private val cacheListeners = mutableListOf<(List<Comment>) -> Unit>()
    private var cachedComments: List<Comment>? = null
    override suspend fun process(arguments: OperationArguments): Document {
        val entry = arguments.entry
        val document = arguments.document

        if (entry == null) {
            return document;
        }

        val layerLevel = MutableStateFlow(0)
        val website = document.getWebsite() ?: return document

        progress(Lang.saveCommentsOperationFetchComments)
        val id = entry.id ?: return document
        val api = betterPublicKmtt(website)

        val commentList = try {
            cachedComments ?: api.comments.getEntryComments(id, SortingType.POPULAR)
                .also { cache ->
                    callListeners(cache)
                }
        } catch (ex: Exception) {
            logger.error(ex) {
                "Caught exception during getting comments in SaveCommentsOperation. Aborting SaveComments operation silently..."
            }

            null
        } ?: return document


        var currentLayer = commentList
            .toTree()
            .associateWith {
                val isAuthorOfPost = entry.author?.id == it.value.author?.id
                createNodeHTML(
                    comment = it,
                    hideColor = null,
                    disableInvisibleHide = layerLevel.value > maxLayerHideOffset,
                    isAuthor = isAuthorOfPost
                ) to randomColor(
                    colorRange, colorRange, colorRange
                )
            }.also {
                layerLevel.update { it + 1 }
            }

        val counter = layerLevel.onEach {
            progress(Lang.saveCommentsOperationParsingCommentsLayers.format(it))
            yield()
        }.launchIn(CoroutineScope(currentCoroutineContext()))

        val toAdd = currentLayer.values

        while (currentLayer.isNotEmpty()) {
            val nextLayer = mutableMapOf<CommentNode, Pair<Element, RGB>>()

            for ((node, pair) in currentLayer) {
                val level = layerLevel.value
                val element = pair.first
                val color = pair.second

                val nodesDiv = element
                    .select(".comment-node .nodes")
                    .first() ?: throw IllegalArgumentException("No nodes element")

                val randomColor = randomColor()

                val htmlChildren = node.children.associateWith {
                    val isAuthorOfPost = entry.author?.id == it.value.author?.id

                    val htmlNode = createNodeHTML(
                        comment = it,
                        hideColor = "#${randomColor.toHex()}",
                        disableInvisibleHide = level > maxLayerHideOffset,
                        isAuthor = isAuthorOfPost
                    )
                    nodesDiv.appendChild(htmlNode)
                    htmlNode to offsetRandomColor(
                        color = color,
                        offset = offset,
                        colorAmountOffset = 2,
                        colorRange = colorRange
                    )
                }

                nextLayer += htmlChildren
            }

            currentLayer = nextLayer
            layerLevel.update { it + 1 }
        }

        counter.cancelAndJoin()
        progress(Lang.saveCommentsOperationSavingChanges)

        val wrapper = document
            .getElementsByClass("savedtf-wrapper")
            .first() ?: throw IllegalArgumentException("No body wrapper")

        wrapper.appendChild(Element("div").apply {
            addClass("comments-list")
            appendChildren(toAdd.map { it.first })
        })

        return document
    }

    private fun createNodeHTML(
        comment: CommentNode,
        hideColor: String?,
        disableInvisibleHide: Boolean,
        isAuthor: Boolean
    ): Element {
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

        // If author is creator of post, then add postOP class
        if (isAuthor) {
            nickname.addClass("postOP")
        }

        // Set id of comment author
        comment.value.author?.id?.let {
            nickname.attr("user-id", it.toString())
        }

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
            hide.attr("style", "background: $hideColor")
        } else {
            hide.addClass("hide--disabled")
        }

        val invisibleHide = commentNode
            .select(".hide--invisible")
            .first()

        if (invisibleHide != null && disableInvisibleHide) {
            invisibleHide.addClass("hide--disabled")
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
        val newText = text.replace(KmttRegex.urlRegex) {
            "<a href=\"${it.value}\">${it.value}</a>"
        }
        element.html(newText)
    }

    private fun callListeners(cache: List<Comment>) {
        cacheListeners.forEach {
            it(cache)
        }
    }

    fun setCachedComments(comments: List<Comment>) {
        cachedComments = comments
    }

    fun addCacheListener(listener: (List<Comment>) -> Unit) {
        cacheListeners += listener
    }
}