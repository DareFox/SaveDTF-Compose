package util.kmttapi

import kmtt.models.enums.Website

object UrlUtil {
    fun isUserProfile(url: String): Boolean {
        return SharedRegex.userProfileLinkRegex.find(url) != null
    }

    fun isEntry(url: String): Boolean {
        return SharedRegex.entryUrlRegex.find(url) != null
    }

    fun isBookmarkLink(url: String): Boolean {
        return SharedRegex.bookmarksRegex.find(url) != null
    }

    fun getWebsiteType(url: String): Website? {
        return when (SharedRegex.websiteRegex.find(url)?.value) {
            "dtf" -> Website.DTF
            "tjournal" -> Website.TJ
            "vc" -> Website.VC
            else -> null
        }
    }

    /**
     * Get media id from Leonardo (media service). Returns `null` if url isn't valid
     *
     * @sample getIdOrNullSample
     */
    fun getMediaIdOrNull(url: String): String? {
        val regex = Regex("""(?<=leonardo\.osnova\.io\/)\S+?[^\/](?=\/|${'$'}|\s+)""")
        val search = regex.find(url)
        return search?.value
    }

    private fun getIdOrNullSample() {
        val validURL = "https://leonardo.osnova.io/118ce574-1507-5a19-92a0-6b06da18fdf3/"
        UrlUtil.getMediaIdOrNull(validURL) // 118ce574-1507-5a19-92a0-6b06da18fdf3

        val noSlashURL = "https://leonardo.osnova.io/118ce574-1507-5a19-92a0-6b06da18fdf3"
        UrlUtil.getMediaIdOrNull(noSlashURL) // 118ce574-1507-5a19-92a0-6b06da18fdf3

        val invalidURL = "https://osnova.io/118ce574-1507-5a19-92a0-6b06da18fdf3"
        UrlUtil.getMediaIdOrNull(invalidURL) // null
    }

    /**
     * Get media id from Leonardo (media service). Throws [IllegalArgumentException] if url isn't valid
     *
     * @sample getIdSample
     */
    fun getMediaId(url: String): String {
        val value = this.getMediaIdOrNull(url)

        requireNotNull(value) {
            "$this is not a valid leonardo.osnova.io (media) url "
        }

        return value
    }

    private fun getIdSample() {
        val validURL = "https://leonardo.osnova.io/118ce574-1507-5a19-92a0-6b06da18fdf3/"
        UrlUtil.getMediaId(validURL) // 118ce574-1507-5a19-92a0-6b06da18fdf3

        val noSlashURL = "https://leonardo.osnova.io/118ce574-1507-5a19-92a0-6b06da18fdf3"
        UrlUtil.getMediaId(noSlashURL) // 118ce574-1507-5a19-92a0-6b06da18fdf3

        val invalidURL = "https://osnova.io/118ce574-1507-5a19-92a0-6b06da18fdf3"
        UrlUtil.getMediaId(invalidURL) // IllegalArgumentException
    }


    fun getProfileID(url: String): Long {
        val id = SharedRegex.userIdRegex.find(url)

        requireNotNull(id) {
            "Got null instead of user id, from user profile url $url"
        }

        return id.value.toLong()
    }

    /**
     * Check if url is
     *
     * "dtf|vc|tjournal.ru/sitemap"
     */
    fun isSitemapAll(url: String): Boolean {
        return SharedRegex.sitemapAll.find(url) != null
    }

    /**
     * Check if url is empty website
     *
     * dtf.ru/ for example
     */
    fun isEmptyWebsite(url: String): Boolean {
        return SharedRegex.emptyWebsiteUrl.find(url) != null
    }

    /**
     * Check if url is sitemap period
     *
     * "dtf|vc|tjournal.ru/sitemap/year-xxxx-xx-xx"
     */
    fun isPeriodSitemap(url: String): Boolean {
        return SharedRegex.sitemapPeriod.find(url) != null
    }

    /**
     * Extract period from sitemap url
     *
     * "dtf|vc|tjournal.ru/sitemap/year-xxxx-xx-xx" -> "year-xxxx-xx-xx"
     */
    fun extractPeriod(url: String): String? = SharedRegex.sitemapExtractPeriod.find(url)?.value

    fun extractPeriodAndFormat(url: String): String? {
        val period = extractPeriod(url) ?: return null

        val year = """(?<=year-)\d{4}""".toRegex().find(period) ?: return null
        val month = """(?<=-)\d{2}(?=-)""".toRegex().find(period) ?: return null
        val day = """\d{2}${'$'}""".toRegex().find(period) ?: return null

        return "${day.value}.${month.value}.${year.value}"
    }
}