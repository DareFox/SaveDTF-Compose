package shared.util.string

import shared.Version

fun String.toVersionOrNull(): Version? {
    val regex = """\d+\.\d+\.\d+""".toRegex()

    val version = regex.find(this)?.value

    return version?.let {
        val major = """^\d+""".toRegex().find(it)?.value?.toIntOrNull()
        val minor = """(?<=\.)\d+(?=\.)""".toRegex().find(it)?.value?.toIntOrNull()
        val fix = """\d+$""".toRegex().find(it)?.value?.toIntOrNull()

        if (major != null && minor != null && fix != null) {
            return Version(major, minor, fix)
        } else {
            return null
        }
    }
}