package util

object SharedRegex {
    // ${'$'} is needed to escape $ symbol in kotlin
    val entryUrlRegex = """(dtf|vc|tjournal)\.ru\/(u\/|).+?\/.+?(?=\/|${'$'}|\s+)""".toRegex(RegexOption.IGNORE_CASE)

    /**
     * Get user id from url
     *
     * @sample userIdRegexUsage
     */
    val userIdRegex =
        """((?<=dtf\.ru\/u\/)|(?<=vc\.ru\/u\/)|(?<=tjournal\.ru\/u\/))\d+""".toRegex(RegexOption.IGNORE_CASE)

    private fun userIdRegexUsage() {
        val userProfile = "https://dtf.ru/u/68409-princessa-ada"
        SharedRegex.userIdRegex.find(userProfile)?.value // returns 68409

        val postInUserBlog = "https://dtf.ru/u/68409-princessa-ada/1170862-posmotrel-betmena"
        SharedRegex.userIdRegex.find(postInUserBlog)?.value // returns 68409

        val postInSubsite = "https://dtf.ru/music/1170937-lovejoy-concrete"
        SharedRegex.userIdRegex.find(postInSubsite)?.value // returns null
    }

    /**
     * Check if url is only user profile
     *
     * @sample userProfileLinkRegexUsage
     */
    val userProfileLinkRegex = """(dtf|vc|tjournal)\.ru\/u\/[^\/]+(\/${'$'}|${'$'})""".toRegex(RegexOption.IGNORE_CASE)

    private fun userProfileLinkRegexUsage() {
        val userProfile = "https://dtf.ru/u/68409-princessa-ada"
        SharedRegex.userProfileLinkRegex.find(userProfile)?.value // returns "dtf.ru/u/68409-princessa-ada"

        val postInUserBlog = "https://dtf.ru/u/68409-princessa-ada/1160705-zagadka-ot-komiteta"
        SharedRegex.userProfileLinkRegex.find(postInUserBlog)?.value // returns null

        val postInSubsite = "https://dtf.ru/music/1161771-c418-the-end"
        SharedRegex.userProfileLinkRegex.find(postInUserBlog)?.value // returns null
    }

    /**
     * Get website from URL
     *
     * Returns `"dtf"`, `"vc"`, `"tjournal"` or `null`
     */
    val websiteRegex = """(dtf|vc|tjournal)(?=\.ru)""".toRegex(RegexOption.IGNORE_CASE)

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
}