package util

object SharedRegex {
    // ${'$'} is needed to escape $ symbol in kotlin
    val entryUrlRegex = """(dtf|vc|tjournal)\.ru\/(u\/|).+?\/.+?(?=\/|${'$'}|\s+)""".toRegex(RegexOption.IGNORE_CASE)

    private val userIdRegex =
        """((?<=dtf\.ru\/u\/)|(?<=vc\.ru\/u\/)|(?<=tjournal\.ru\/u\/))\d+""".toRegex(RegexOption.IGNORE_CASE)

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
     * https://stackoverflow.com/a/45615798/13494106
     *
     * God bless his soul
     */
    val filenameValidationRegex =
        """\A(?!(?:COM[0-9]|CON|LPT[0-9]|NUL|PRN|AUX|com[0-9]|con|lpt[0-9]|nul|prn|aux)|[\s\.])[^\\\/:*"?<>|]{1,254}\z""".toRegex()
}