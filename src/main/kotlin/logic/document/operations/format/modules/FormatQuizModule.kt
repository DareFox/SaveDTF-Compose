package logic.document.operations.format.modules

import kmtt.models.QuizResult
import kmtt.models.enums.Website
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import util.dom.getWebsite
import util.kmttapi.SharedRegex
import util.kmttapi.betterPublicKmtt

object FormatQuizModule : IHtmlFormatModule {
    override suspend fun process(document: Document): Document {
        val website = document.getWebsite()
        val quizes = document.getElementsByClass("quiz")

        quizes.forEach {
            deleteButtons(it)
            addVotes(it, website)
        }


        return document
    }

    private suspend fun addVotes(element: Element, website: Website?) {
        val hash = element.attr("data-quiz-hash")
        var result: QuizResult? = try {
            website?.let {
                betterPublicKmtt(it).quiz.getResults(hash)
            }
        } catch (ex: Exception) {
            null
        }

        val quizItems = element.getElementsByClass("quiz__item")

        quizItems.forEach { quizItem ->
            val itemId = quizItem.classNames().firstNotNullOfOrNull {
                SharedRegex.quizItemHash.find(it)?.value
            }

            val votes = result?.items?.get(itemId)

            quizItem.getElementsByClass("quiz__item__result").forEach {
                it.remove()
            }

            val progressBarContainer = Element("div").also {
                it.addClass("quiz__progressbar__container")
            }

            val progressBarMainline = Element("div").also {
                it.addClass("quiz__progressbar__mainline")
                progressBarContainer.appendChild(it)

                if (votes != null) {
                    it.attr("style", "width: ${votes.percentage}%;")

                    if (votes.isWinner) {
                        it.addClass("quiz__progressbar__winner")
                    }
                }
            }

            val progressBarUnderline = Element("div").also {
                it.addClass("quiz__progressbar__underline")
                progressBarContainer.appendChild(it)

                if (votes?.isWinner == true) {
                    it.addClass("quiz__progressbar__winner")
                }
            }

            val percentage = if (votes != null) "${votes.percentage}% (${votes.count})" else "???"
            addPercentage(quizItem, percentage)

            quizItem.appendChild(progressBarContainer)
        }
    }

    private fun addPercentage(element: Element, percentage: String) {
        val label = element.getElementsByClass("quiz__item__label")

        label.first()?.also {
            it.attr("style", "display: flex; justify-content: space-between;")
            it.getElementsByClass("quiz__item__label__percent")
                .first()
                ?.text(percentage)
                ?.attr("style", "width: 25%; text-align: end;")
        }
    }

    private fun deleteButtons(element: Element) {
        element.getElementsByClass("quiz__panel").forEach {
            it.remove()
        }

        element.getElementsByClass("quiz__loader").forEach {
            it.remove()
        }
    }
}