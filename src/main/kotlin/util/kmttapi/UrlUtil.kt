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

    fun getProfileID(url: String): Long {
        val id = SharedRegex.userIdRegex.find(url)

        requireNotNull(id) {
            "Got null instead of user id, from user profile url $url"
        }

        return id.value.toLong()
    }
}