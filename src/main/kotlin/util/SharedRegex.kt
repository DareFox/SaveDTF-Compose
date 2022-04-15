package util

object SharedRegex {
    // ${'$'} is needed to escape $ symbol in kotlin
    val entryUrlRegex = """(dtf|vc|tjournal)\.ru\/(u\/|).+?\/.+?(?=\/|${'$'}|\s+)""".toRegex(RegexOption.IGNORE_CASE)

    val websiteRegex = """(dtf|vc|tjournal)(?=\.ru)""".toRegex(RegexOption.IGNORE_CASE)
}