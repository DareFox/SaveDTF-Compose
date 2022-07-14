package util

import me.darefox.saveDTF_compose.BuildConfig

fun getCrashLogReport(ex: Throwable): String {
    val stringBuilder = StringBuilder()
    
    stringBuilder.title("PROGRAM VERSION")
    stringBuilder.append("""
        SaveDTF_Version = ${BuildConfig.APP_FULL_VERSION}
        SaveDTF_SemanticVersion = ${BuildConfig.APP_SEMANTIC_VERSION}
        SaveDTF_BuildNumber = ${BuildConfig.APP_BUILD_NUMBER}
        SaveDTF_Is_Dev_Version = ${BuildConfig.IS_DEV_VERSION}
    """.trimIndent())

    stringBuilder.title("SYSTEM INFO")
    stringBuilder.append("""
        os.name = ${System.getProperty("os.name")}
        java.version = ${System.getProperty("java.version")}
    """.trimIndent())

    stringBuilder.title("STACKTRACE")
    stringBuilder.append(ex.stackTraceToString())

    return stringBuilder.toString().trim()
}

private fun StringBuilder.title(title: String) {
    this.append("\n\n-=-=- $title -=-=-\n")
}