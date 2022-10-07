import java.util.*


plugins {
    kotlin("jvm") version "1.7.10"
    id("java")
}

group = "me.darefox"
version = "2.0.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

val ktorVersion = "1.6.8"


dependencies {
    // Kotlin Serialization. For caching and gallery formating
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")

    // Http-client Ktor. For downloading media from servers
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-serialization:$ktorVersion")

    // API for cmtt websites
    implementation("com.github.DareFox:kmttAPI:0.3.6")

    // HTML Parser
    implementation("org.jsoup:jsoup:1.14.3")

    // Function rate limiter
    implementation("io.github.resilience4j:resilience4j-ratelimiter:1.7.1")
    implementation("io.github.resilience4j:resilience4j-kotlin:1.7.1")

    // Logger
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.21")
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("ch.qos.logback:logback-core:1.2.11")

    // Tester
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

/**
 * Delete previous generated code
 */
val generated = file("build/generated/")
if (!generated.deleteRecursively()) {
    println("CAN'T DELETE /build/generated! THIS MAY CAUSE SOME ISSUES SUCH AS IRRELEVANT BUILD CONFIG")
} else {
    println("Successfully deleted /build/generated before generating new code")
}

/**
 * L10n tasks
 */

// Fallback language for proxy
val defaultLang = "en_US"
val registryName = "Languages"

val tagField = "LANG_TAG"
val nameField = "LANG_NAME"

val allLanguages = getAllLanguageProperties()
val defaultLangProperties = allLanguages.firstOrNull() {
    println(it)
    it.containsKey(tagField) && it[tagField] == defaultLang
} ?: throw IllegalArgumentException("No default language was detected with $tagField $defaultLang")

// Package to save all generated classes
val classPackage = "shared.i18n.langs"

// Create build/generated folder
// Also remove previous generated files
val generatedSourceDir = file("build/generated/language/kotlin/").also { dir ->
    dir.deleteRecursively()
    dir.mkdirs()
}
val generatedInterface = generateInterface(defaultLangProperties)

val generatedProxy = generateProxyClass(generatedInterface)

val generatedLanguageImpl = allLanguages.map {
    // Give default language typealias
    val typeAlias = if (it == defaultLangProperties) {
        "DefaultLanguageResource"
    } else null

    generateLanguageClass(it, generatedInterface, typeAlias)
}

// Save generated interface code
generatedSourceDir.resolve("${generatedInterface.className}.kt").writeText(
    generatedInterface.code
)


// Create list for generated languages
generatedSourceDir.resolve("langList.kt").let {
    val codeBuilder = StringBuilder(200)

    codeBuilder.append("package $classPackage")
    codeBuilder.append("\nval AvailableLanguages = listOf<${generatedInterface.className}>(\n")
    codeBuilder.append(generatedLanguageImpl.joinToString(",\n") {
        "\t" + it.className
    })
    codeBuilder.append("\n)")

    it.writeText(codeBuilder.toString())
}
// Save generated proxy class
generatedSourceDir.resolve("${generatedProxy.className}.kt").writeText(
    generatedProxy.code
)

// Save generated lang implementations
generatedLanguageImpl.forEach {

    generatedSourceDir.resolve("${it.className}.kt").writeText(it.code)
}

// Make generated code visible from root of the project
kotlin {
    sourceSets["main"].apply {
        kotlin.srcDir("build/generated/language/kotlin")
    }
}

data class GeneratedInterface(val code: String, val className: String, val listOfKeys: List<String>)

fun generateInterface(properties: java.util.Properties): GeneratedInterface {
    val codeBuilder = StringBuilder(2000)
    val keys = mutableListOf<String>()

    val tagFieldValue = properties[tagField]
    val nameFieldValue = properties[nameField]

    codeBuilder.append("package $classPackage")
    codeBuilder.append("\nsealed interface LanguageResource {")
    // We
    codeBuilder.append("\n\tval localeTag: String")
    codeBuilder.append("\n\tval localeName: String")
    properties.forEach { k, v ->
        // create string field on each key & value pair
        val value = v.toString()
        val key = k.toString()

        if (key == tagField || key == nameField)  return@forEach

        val comment = """
         /**
         * ### Default value ($tagFieldValue, $nameFieldValue):
         *
         * ```
         * $value
         * ```
         *
         */
        """.trimIndent()

        codeBuilder.append("\n", comment)
        codeBuilder.append("\n\tval $key: String").also {
            keys += key
        }
    }

    codeBuilder.append("\n}")
    return GeneratedInterface(codeBuilder.toString(), "LanguageResource", keys)
}
data class GeneratedProxy(val code: String, val className: String, val baseInterface: GeneratedInterface)

fun generateProxyClass(base: GeneratedInterface, className: String = "Proxy" + base.className): GeneratedProxy {
    val codeBuilder = StringBuilder(2000)
    val baseInterface = base.className

    codeBuilder.append("package $classPackage")
    codeBuilder.append("\nclass $className (val current: $baseInterface, val default: $baseInterface): $baseInterface {")
    codeBuilder.append("\n\toverride val localeTag: String = current.localeTag")
    codeBuilder.append("\n\toverride val localeName: String = current.localeName")

    base.listOfKeys.forEach {
        // TODO: Provide convenient way to check metadata fields
        if (it == tagField || it == nameField) return@forEach

        codeBuilder.append("override val $it: String".tabStart(1))

        codeBuilder.append("get() = try {".tabStart(2))
        codeBuilder.append("current.$it".tabStart(3))

        // Catching Error instead of Exception
        // because TO-DO function throws Error, not Exception (duh)
        codeBuilder.append("} catch (_: Error) {".tabStart(2))

        // If current language fails (e.g no translation), then use default language
        codeBuilder.append("default.$it".tabStart(3))
        codeBuilder.append("}".tabStart(2))
    }

    codeBuilder.append("\n}")
    return GeneratedProxy(codeBuilder.toString(), className, base)
}

data class GeneratedLanguageImpl(val code: String, val className: String, val baseInterface: GeneratedInterface)

fun generateLanguageClass(
    languageProperties: java.util.Properties,
    base: GeneratedInterface,
    aliasType: String? = null
) : GeneratedLanguageImpl {
    val codeBuilder = StringBuilder(2000)
    val baseInterface = base.className
    val name = languageProperties[tagField]
    val className = "${name}LanguageResource"

    codeBuilder.append("package $classPackage")

    if (aliasType != null) {
        codeBuilder.append("\n\ntypealias $aliasType = $className")
    }

    codeBuilder.append("\n\nobject $className: $baseInterface {")

    codeBuilder.append("\n\toverride val localeTag: String = " +
            "\"${languageProperties[tagField] ?: throw IllegalArgumentException("No locale tag in $tagField field")}\"")

    codeBuilder.append("\n\toverride val localeName: String = " +
            "\"${languageProperties[nameField] ?: throw IllegalArgumentException("No locale name in $nameField field ($className)")}\"")

    base.listOfKeys.forEach {
        val translationValue = languageProperties[it]?.toString()
        val valueForInsertion = if (translationValue == null) {
            "TODO(\"$name doesn't have translation for $it key\")"
        } else {
            "\"${translationValue.escapeAll()}\""
        }

        codeBuilder.append("override val $it: String".tabStart(1))
        codeBuilder.append("get() = $valueForInsertion".tabStart(2))
    }

    codeBuilder.append("\n}")

    return GeneratedLanguageImpl(codeBuilder.toString(), className, base)
}

fun getAllLanguageProperties(): List<java.util.Properties> {
    val folder = file("src/main/resources/l10n")
    val properties = mutableListOf<java.util.Properties>()

    folder.walk().forEach { file ->
        if (!file.isFile || file.extension != "properties") return@forEach

        properties += Properties().also { it.load(file.bufferedReader()) }
    }

    return properties
}

fun String.escapeAll(): String {
    return this.replace("\"", "\\\"").replace("\n","\\n")
}

/**
 * Add number of tabs at start of string
 */
fun String.tabStart(num: Int, newLine: Boolean = true): String {
    val newLineChar = if (newLine) "\n" else ""

    return newLineChar + "\t".repeat(num) + this
}
