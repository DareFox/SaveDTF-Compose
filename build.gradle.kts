import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    id("org.jetbrains.compose") version "1.1.1"
    id("org.openjfx.javafxplugin") version "0.0.10"
    id("com.github.gmazzo.buildconfig") version "3.1.0"
}

repositories {
    google()
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}


group = "me.darefox"

// DO NOT CHANGE IT!
// THIS UUID IS USED FOR UPGRADING FEATURE AND SHOULD REMAIN CONSTANT
// MORE HERE: https://github.com/JetBrains/compose-jb/tree/master/tutorials/Native_distributions_and_local_execution#platform-specific-options
val DO_NOT_CHANGE_THIS_UUID = "71454f6a-55e9-44d8-830b-59ca8fc9f418"

// CHANGE VERSION HERE
val versionObject = BuildVersion(2, 0, 0, 0)
val currentVersion = getBuildVersion(false)

version = currentVersion.convertToSemanticVersion()


val ktorVersion = "1.6.8"

dependencies {
    implementation(compose.desktop.currentOs)

    // ICONS
    implementation("br.com.devsrsouza.compose.icons.jetbrains:feather:1.0.0")

    // Kotlin Serialization. For caching
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")

    // Http-client Ktor. For downloading media from servers
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-serialization:$ktorVersion")

    // API for cmtt websites
    implementation("com.github.DareFox:kmttAPI:0.3.3")

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

}

javafx {
    version = "17.0.1"
    modules = listOf("javafx.swing")
}

var increment = true
var isDevVersion = true

tasks.withType {
    // if we're building packages, then don't increment build
    if (gradle.startParameter.taskNames.contains("package")) {
        increment = false
    }

    isDevVersion = project.properties["buildType"] != "release"
}

buildConfig {
    val version = getBuildVersion(increment)

    buildConfigField("String", "APP_FULL_VERSION", "\"${version}\"")
    buildConfigField("String", "APP_SEMANTIC_VERSION", "\"${version.convertToSemanticVersion()}\"")
    buildConfigField("kotlin.Long", "APP_BUILD_NUMBER", version.build.toString())
    buildConfigField("kotlin.Boolean", "IS_DEV_VERSION", "$isDevVersion")
}



tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

val iconsRoot = project.file("src/main/resources/img")

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)

            packageName = "SaveDTF"
            description = "SaveDTF"
            vendor = "DareFox"

            windows {
                iconFile.set(iconsRoot.resolve("DTF_logo.ico"))
                packageVersion = currentVersion.convertToSemanticVersion()
                dirChooser = true
                upgradeUuid = DO_NOT_CHANGE_THIS_UUID
                menuGroup = "Programs" // Make it searchable in Start menu
            }
            linux {
                iconFile.set(iconsRoot.resolve("DTF_logo.png"))
                debMaintainer = "easymalink@gmail.com"
                appCategory = "net"
                packageVersion = currentVersion.convertToSemanticVersion() + "+${currentVersion.build}"

            }
            macOS {
                packageVersion = currentVersion.convertToSemanticVersion()
                iconFile.set(iconsRoot.resolve("DTF_logo.icns"))
            }

            modules(
                "java.instrument",
                "java.management",
                "java.naming",
                "java.prefs",
                "jdk.jfr",
                "jdk.unsupported",
                "jdk.unsupported.desktop"
            )
        }
    }
}

data class BuildVersion(val major: Int, val minor: Int, val patch: Int, val build: Long) {
    fun convertToSemanticVersion(): String = "$major.$minor.$patch"

    override fun toString(): String {
        return "$major.$minor.$patch ($build)"
    }
}

fun getBuildVersion(increment: Boolean): BuildVersion {
    val previous = getPreviousVersion()
    val newVersion: BuildVersion = when {
        previous == null ||
                previous.major != versionObject.major ||
                previous.minor != versionObject.minor ||
                previous.patch != versionObject.patch -> versionObject.copy(build = 0L)
        else -> previous.copy(build = if (increment) previous.build + 1 else previous.build)
    }

    return newVersion.also { saveVersion(it) }
}

fun getPreviousVersion(): BuildVersion? {
    val propFile = file("version.properties")

    if (!propFile.canRead()) {
        throw IllegalAccessException("Cannot read version.properties")
    }

    val properties = Properties().also { it.load(propFile.bufferedReader()) }

    return try {
        val major = properties["MAJOR_VERSION"].toString().toInt()
        val minor = properties["MINOR_VERSION"].toString().toInt()
        val patch = properties["PATCH_VERSION"].toString().toInt()
        val build = properties["BUILD_NUMBER"].toString().toLong()

        BuildVersion(major, minor, patch, build)
    } catch(ex: Exception) {
        println(ex.toString())
        null
    }
}

fun saveVersion(version: BuildVersion) {
    val propFile = file("version.properties").also { it.createNewFile() }
    val properties = Properties().also { it.load(propFile.bufferedReader()) }

    properties["MAJOR_VERSION"] = version.major.toString()
    properties["MINOR_VERSION"] = version.minor.toString()
    properties["PATCH_VERSION"] = version.patch.toString()
    properties["BUILD_NUMBER"] = version.build.toString()

    properties.store(propFile.bufferedWriter(), "Previous build version")
}

