package util.kmttapi

object SharedRegex {
    // ${'$'} is needed to escape $ symbol in kotlin
    val entryUrlRegex =
        """(dtf|vc|tjournal)\.ru\/((u\/|).+?\/.+?|\d+.*)(?=\/|${'$'}|\s+)""".toRegex(RegexOption.IGNORE_CASE)

    /**
     * Get user id from url
     *
     * @sample userIdRegexUsage
     */
    val userIdRegex =
        """((?<=dtf\.ru\/u\/)|(?<=vc\.ru\/u\/)|(?<=tjournal\.ru\/u\/))\d+""".toRegex(RegexOption.IGNORE_CASE)

    private fun userIdRegexUsage() {
        val userProfile = "https://dtf.ru/u/68409-princessa-ada"
        userIdRegex.find(userProfile)?.value // returns 68409

        val postInUserBlog = "https://dtf.ru/u/68409-princessa-ada/1170862-posmotrel-betmena"
        userIdRegex.find(postInUserBlog)?.value // returns 68409

        val postInSubsite = "https://dtf.ru/music/1170937-lovejoy-concrete"
        userIdRegex.find(postInSubsite)?.value // returns null
    }

    /**
     * Check if url is only user profile
     *
     * @sample userProfileLinkRegexUsage
     */
    val userProfileLinkRegex = """(dtf|vc|tjournal)\.ru\/u\/[^\/]+(\/${'$'}|${'$'})""".toRegex(RegexOption.IGNORE_CASE)

    private fun userProfileLinkRegexUsage() {
        val userProfile = "https://dtf.ru/u/68409-princessa-ada"
        userProfileLinkRegex.find(userProfile)?.value // returns "dtf.ru/u/68409-princessa-ada"

        val postInUserBlog = "https://dtf.ru/u/68409-princessa-ada/1160705-zagadka-ot-komiteta"
        userProfileLinkRegex.find(postInUserBlog)?.value // returns null

        val postInSubsite = "https://dtf.ru/music/1161771-c418-the-end"
        userProfileLinkRegex.find(postInUserBlog)?.value // returns null
    }

    /**
     * Get quiz item hash
     *
     * Returns hash of quiz item
     *
     * @sample quizItemHashUsage
     */
    val quizItemHash = """(?<=quiz__item--)\S*""".toRegex(RegexOption.IGNORE_CASE)

    private fun quizItemHashUsage() {
        val attribute = "quiz__item--a16495916020"
        quizItemHash.find(attribute)?.value // returns "a16495916020"
    }

    /**
     * Get website from URL
     *
     * Returns `"dtf"`, `"vc"`, `"tjournal"` or `null`
     */
    val websiteRegex = """(dtf|vc|tjournal)(?=\.ru)""".toRegex(RegexOption.IGNORE_CASE)

    /**
     * Check if website is empty
     *
     * Example:
     * dtf.ru/ is empty
     * dtf.ru/u/123 is NOT empty
     *
     * https://dtf.ru/ is empty too
     */
    val emptyWebsiteUrl =
        """^(https://|http://|www\.|)(dtf|vc|tjournal)\.ru(/|)${'$'}""".toRegex(RegexOption.IGNORE_CASE)

    /**
     * Check if link is **.ru/sitemap only
     *
     * Example:
     * dtf.ru/sitemap is sitemap to all entries
     * dtf.ru/sitemap/year-2022-02-02 is NOT*
     */
    val sitemapAll =
        """^(https://|http://|www\.|)(dtf|vc|tjournal)\.ru/sitemap(/|)${'$'}""".toRegex(RegexOption.IGNORE_CASE)

    /** Check if link is sitemap period
     *
     * Example:
     * https://dtf.ru/sitemap/year-2022-08-01 is period
     * https://dtf.ru/sitemap/ is NOT
     */
    val sitemapPeriod =
        """^(https://|http://|www\.|)(dtf|vc|tjournal)\.ru/sitemap/year-\d{4}-\d{2}-\d{2}""".toRegex(RegexOption.IGNORE_CASE)

    /**
     * Extract sitemap period:
     *
     * Example:
     * https://dtf.ru/sitemap/year-2022-08-01 -> year-2022-08-01
     * https://dtf.ru/sitemap -> null
     */
    val sitemapExtractPeriod = """(?<=sitemap/)year-\d{4}-\d{2}-\d{2}""".toRegex(RegexOption.IGNORE_CASE)

    /**
     * Check bookmark URL
     *
     * Returns `(dtf/vc/tjournal).ru/bookmarks` or `null`
     */
    val bookmarksRegex = """(dtf|vc|tjournal)\.ru\/bookmarks""".toRegex(RegexOption.IGNORE_CASE)

    /**
     * Check if filename is valid
     */
    val filenameValidationRegex =
        // https://www.oreilly.com/library/view/regular-expressions-cookbook/9781449327453/ch08s25.html
        """([\\\/:"*?<>|]+|(\.\s*)*${'$'})""".toRegex()

    val urlRegex =
        "[(http(s)?):\\/\\/(www\\.)?a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)".toRegex(
            setOf(RegexOption.MULTILINE, RegexOption.IGNORE_CASE)
        )
}